package ru.iris.commons.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.dao.DeviceValueDAO;
import ru.iris.commons.database.dao.DeviceValueHistoryDAO;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.database.DeviceValueChange;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeviceRegistryImpl implements DeviceRegistry {
    private final Map<String, Device> registry = new ConcurrentHashMap<>();
    private final Set<DeviceValueChange> changes = ConcurrentHashMap.newKeySet();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceDAO deviceDAO;

    @Autowired
    private DeviceValueDAO deviceValueDAO;

    @Autowired
    private DeviceValueHistoryDAO deviceValueHistoryDAO;

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @Transactional
    public void init() {
        List<Device> devices = deviceDAO.findAll();
        addOrUpdateDevices(devices);
        registry.values().forEach(device -> {
            device.getValues().values().forEach(deviceValue -> deviceValue.setCurrentValue(null));
        });
    }

    @Override
    @Transactional
    public Device saveDeviceToDatabase(Device device) {
        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return null;
        }

        device = deviceDAO.save(device);
        registry.put(getIdent(device), device);
        return device;
    }

    @Override
    @Transactional
    public Device addOrUpdateDevice(Device device) {
        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return null;
        }

        device = saveDeviceToDatabase(device);
        return device;
    }

    @Override
    @Transactional
    public void addOrUpdateDevices(List<Device> devices) {
        devices.forEach(device -> {
            if (device != null) {
                saveDeviceToDatabase(device);
            }
        });
    }

    @Override
    public DeviceValue addChange(DeviceValue value) {
        DeviceValueChange add = new DeviceValueChange();
        add.setDeviceValue(value);
        add.setValue(value.getCurrentValue());
        try {
            add.setAdditionalData(objectMapper.writeValueAsString(value.getAdditionalData()));
        } catch (JsonProcessingException ignored) {
        }
        add.setDate(new Date());

        value.setLastUpdated(new Date());
        value.getChanges().add(add);

        changes.add(add);

        return value;
    }

    @Override
    @Transactional
    public DeviceValue addChange(Device device, String key, String level, ValueType type) {
        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return null;
        }

        DeviceValue value = device.getValues().get(key);

        if (value == null) {
            value = new DeviceValue();
            value.setDevice(device);
            value.setName(key);
            value.setType(type);
            value.setUnits("unknown");
            value.setReadOnly(false);

            value = deviceValueDAO.save(value);
        }

        if (value.getCurrentValue() != null) {
            if (!value.getCurrentValue().equals(level) || type.equals(ValueType.TRIGGER)) {
                value.setCurrentValue(level);
                value = addChange(value);
            }
        } else {
            value.setCurrentValue(level);
            value = addChange(value);
        }

        device.getValues().put(key, value);

        return value;
    }

    @Override
    public List<Device> getDevicesByProto(SourceProtocol proto) {
        return registry
                .values()
                .stream()
                .filter(device -> device.getSource().equals(proto))
                .collect(Collectors.toList());
    }

    @Override
    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>(registry.values().size());
        devices.addAll(registry.values());
        return devices;
    }

    @Override
    public Device getDevice(SourceProtocol protocol, String channel) {
        return registry.get(protocol.name().toLowerCase() + "/channel/" + channel);
    }

    @Override
    public DeviceValue getDeviceValue(SourceProtocol protocol, String channel, String value) {
        Device device = registry.get(protocol.name().toLowerCase() + "/channel/" + channel);

        if (device == null)
            return null;

        return device.getValues().getOrDefault(value, null);
    }

    @Scheduled(initialDelay = 60_000L, fixedDelay = 60_000L)
    void saveChangesJob() {
        synchronized (changes) {
            if(changes.size() > 0) {
                logger.debug("Saving {} changes to database", changes.size());
                changes.forEach(change -> deviceValueHistoryDAO.save(change));
                changes.clear();
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // HISTORY
    /////////////////////////////////////////////////////////////////

    @Override
    public List getHistory(SourceProtocol proto, String channel, String label, Date start) {
        return getHistory(proto, channel, label, start, null);
    }

    @Override
    public List getHistory(SourceProtocol proto, String channel, String label, Date start, Date stop) {
        return getHistory(proto, channel, label, start, stop, true);
    }

    @Override
    public List getHistory(SourceProtocol proto, String channel, String label, Date start, Date stop, boolean desc) {
        Device device = getDevice(proto, channel);

        for (String key : device.getValues().keySet()) {
            if (key.equals(label))
                return getHistory(device.getValues().get(label).getId(), start, stop, desc);
        }

        return new ArrayList();
    }

    private List getHistory(long id, Date startDate, Date stopDate, boolean desc) {
        String SQL = "FROM DeviceValueChange AS c WHERE c.deviceValue.id = :id AND c.date BETWEEN :stDate AND";

        if (stopDate == null) {
            SQL += " current_date order by c.date";
            if (desc) {
                SQL += " desc";
            } else {
                SQL += " asc";
            }

            return em.createQuery(SQL)
                    .setParameter("id", id)
                    .setParameter("stDate", startDate)
                    .getResultList();
        } else {
            SQL += " :enDate order by c.date";
            if (desc) {
                SQL += " desc";
            } else {
                SQL += " asc";
            }

            return em.createQuery(SQL)
                    .setParameter("id", id)
                    .setParameter("stDate", startDate)
                    .setParameter("enDate", stopDate)
                    .getResultList();
        }
    }

    @Override
    @Transactional
    public synchronized void deleteHistory(SourceProtocol proto, String channel, String label, Date from) {
        Device device = getDevice(proto, channel);

        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return;
        }
        for (String key : device.getValues().keySet()) {
            if (key.equals(label)) {
                String SQL = "DELETE FROM DeviceValueChange AS c WHERE c.deviceValue.id = :id AND c.date BETWEEN :stDate AND :enDate";
                em.createQuery(SQL)
                        .setParameter("id", device.getValues().get(key).getId())
                        .setParameter("stDate", new DateTime(from).minusYears(100).toDate())
                        .setParameter("enDate", from)
                        .executeUpdate();

                device = deviceDAO.findById(device.getId()).get();
                saveDeviceToDatabase(device);
            }
        }
    }

    private String getIdent(Device device) {
        return device.getSource().name().toLowerCase() + "/channel/" + device.getChannel();
    }
}
