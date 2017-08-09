package ru.iris.commons.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValue;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.protocol.enums.SourceProtocol;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeviceRegistry {

    private final Gson gson = new GsonBuilder().create();
    private Map<String, Device> registry = new ConcurrentHashMap<>();
    private Map<String, String> humanReadable = new ConcurrentHashMap<>();
    private boolean initComplete = false;

    @Autowired
    private DeviceDAO deviceDAO;

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @Transactional
    public void init() {
        List<Device> devices = deviceDAO.findAll();
        addOrUpdateDevices(devices);
        initComplete = true;
    }

    @Transactional
    public Device addOrUpdateDevice(Device device) {
        if (device == null) {
            logger.error("Device, passed into registry is null!");
            return null;
        }

        String ident = device.getSource().name().toLowerCase() + "/channel/" + device.getChannel();

        if (!humanReadable.containsValue(ident))
            if (device.getHumanReadable() != null && !device.getHumanReadable().isEmpty())
                humanReadable.put(device.getHumanReadable(), ident);

        if (initComplete) {
            device = deviceDAO.save(device);
        }

        registry.put(ident, device);
        return device;
    }

    @Transactional
    public void addOrUpdateDevices(List<Device> devices) {
        devices.forEach(device -> {
            if (device == null) {
                logger.error("Device passed into registry is null!");
                return;
            }

            String ident = device.getSource().name().toLowerCase() + "/channel/" + device.getChannel();

            if (!humanReadable.containsValue(ident))
                if (device.getHumanReadable() != null && !device.getHumanReadable().isEmpty())
                    humanReadable.put(device.getHumanReadable(), ident);

            if (initComplete) {
                device = deviceDAO.save(device);
            }

            registry.put(ident, device);
        });
    }

    public DeviceValue addChange(DeviceValue value) {
        DeviceValueChange add = new DeviceValueChange();
        add.setValue(value.getCurrentValue());
        add.setAdditionalData(gson.toJson(value.getAdditionalData()));
        add.setDate(new Date());

        value.setLastUpdated(new Date());
        value.getChanges().add(add);

        return value;
    }

    public List<Device> getDevicesByProto(SourceProtocol proto) {
        return registry
                .values()
                .stream()
                .filter(device -> device.getSource().equals(proto))
                .collect(Collectors.toList());
    }

    public Device getDevice(SourceProtocol protocol, Short channel) {
        return registry.get(protocol.name().toLowerCase() + "/channel/" + channel);
    }

    public Device getDevice(String humanReadableIdent) {
        String ident = humanReadable.get(humanReadableIdent);

        if (ident != null)
            return registry.get(ident);
        else
            return null;
    }

    public DeviceValue getDeviceValue(SourceProtocol protocol, Short channel, String value) {
        Device device = registry.get(protocol.name().toLowerCase() + "/channel/" + channel);

        if (device == null)
            return null;

        return device.getValues().getOrDefault(value, null);
    }

    public DeviceValue getDeviceValue(String humanReadableIdent, String value) {

        String ident = humanReadable.get(humanReadableIdent);

        if (ident != null) {
            Device device = registry.get(ident);
            return device.getValues().getOrDefault(value, null);
        } else
            return null;
    }

    /////////////////////////////////////////////////////////////////
    // HISTORY
    /////////////////////////////////////////////////////////////////

    public List getHistory(String humanReadableIdent, String label, Date start) {
        String ident = humanReadable.get(humanReadableIdent);

        if (ident != null) {
            Device device = registry.get(ident);
            return getHistory(device.getSource(), device.getChannel(), label, start, null);
        } else
            return null;
    }

    public List getHistory(String humanReadableIdent, String label, Date start, Date stop) {
        String ident = humanReadable.get(humanReadableIdent);

        if (ident != null) {
            Device device = registry.get(ident);
            return getHistory(device.getSource(), device.getChannel(), label, start, stop);
        } else
            return null;
    }

    public List getHistory(SourceProtocol proto, Short channel, String label, Date start) {
        return getHistory(proto, channel, label, start, null);
    }

    public List getHistory(SourceProtocol proto, Short channel, String label, Date start, Date stop) {
        Device device = getDevice(proto, channel);

        for (String key : device.getValues().keySet()) {
            if (key.equals(label))
                return getHistory(device.getValues().get(label).getId(), start, stop);
        }

        return null;
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
