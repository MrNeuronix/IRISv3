package ru.iris.commons.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.dao.DeviceValueDAO;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.database.DeviceValueChange;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeviceRegistryImpl implements DeviceRegistry {

    private final Gson gson = new GsonBuilder().create();
    private Map<String, Device> registry = new ConcurrentHashMap<>();
    private boolean initComplete = false;

    @Autowired
    private DeviceDAO deviceDAO;

    @Autowired
    private DeviceValueDAO deviceValueDAO;

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @Transactional
    public void init() {
        List<Device> devices = deviceDAO.findAll();
        addOrUpdateDevices(devices);
        registry.values().forEach(device -> device.getValues().values().forEach(deviceValue -> deviceValue.setCurrentValue(null)));
        initComplete = true;
    }

    @Override
    @Transactional
    public synchronized Device addOrUpdateDevice(Device device) {
        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return null;
        }

        String ident = device.getSource().name().toLowerCase() + "/channel/" + device.getChannel();

        if (initComplete) {
            device = deviceDAO.save(device);
        }

        registry.put(ident, device);
        return device;
    }

    @Override
    @Transactional
    public synchronized void addOrUpdateDevices(List<Device> devices) {
        devices.forEach(device -> {
            if (device == null) {
                logger.error("Device passed into registry is null!");
                return;
            }

            String ident = device.getSource().name().toLowerCase() + "/channel/" + device.getChannel();

            if (initComplete) {
                device = deviceDAO.save(device);
            }

            registry.put(ident, device);
        });
    }

    @Override
    public synchronized DeviceValue addChange(DeviceValue value) {
        DeviceValueChange add = new DeviceValueChange();
        add.setDeviceValue(value);
        add.setValue(value.getCurrentValue());
        add.setAdditionalData(gson.toJson(value.getAdditionalData()));
        add.setDate(new Date());

        value.setLastUpdated(new Date());
        value.getChanges().add(add);

        return value;
    }

    @Override
    public synchronized DeviceValue addChange(Device device, String key, String level, ValueType type) {
        DeviceValue value = device.getValues().get(key);

        if (value == null) {
            value = new DeviceValue();
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

        value.setDevice(device);
        value.setName(key);
        value.setType(type);
        value.setUnits("unknown");
        value.setReadOnly(false);

        device.getValues().put(key, value);

        addOrUpdateDevice(device);

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
        return new ArrayList<>(registry.values());
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

    /////////////////////////////////////////////////////////////////
    // HISTORY
    /////////////////////////////////////////////////////////////////

    @Override
    public List getHistory(SourceProtocol proto, String channel, String label, Date start) {
        return getHistory(proto, channel, label, start, null);
    }

    @Override
    public List getHistory(SourceProtocol proto, String channel, String label, Date start, Date stop) {
        Device device = getDevice(proto, channel);

        for (String key : device.getValues().keySet()) {
            if (key.equals(label))
                return getHistory(device.getValues().get(label).getId(), start, stop);
        }

        return new ArrayList();
    }

    private List getHistory(long id, Date startDate, Date stopDate) {
        String SQL = "FROM DeviceValueChange AS c WHERE c.deviceValue.id = :id AND c.date BETWEEN :stDate AND";

        if (stopDate == null) {
            SQL += " current_date order by c.date desc";
            return em.createQuery(SQL)
                    .setParameter("id", id)
                    .setParameter("stDate", startDate)
                    .getResultList();
        } else {
            SQL += " :enDate order by c.date desc";
            return em.createQuery(SQL)
                    .setParameter("id", id)
                    .setParameter("stDate", startDate)
                    .setParameter("enDate", stopDate)
                    .getResultList();
        }
    }

}
