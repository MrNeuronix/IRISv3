package ru.iris.commons.registry;

import org.springframework.transaction.annotation.Transactional;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.database.DeviceValueChange;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

import java.util.Date;
import java.util.List;

/**
 * @author Nikolay Viguro, 10.08.17
 */
public interface DeviceRegistry {

    Device saveDeviceToDatabase(Device device);

    @Transactional
    DeviceValueChange saveDeviceChangeToDatabase(DeviceValueChange change);

    Device addOrUpdateDevice(Device device);

    void addOrUpdateDevices(List<Device> devices);

    DeviceValue addChange(DeviceValue value);

    DeviceValue addChange(Device device, String key, String level, ValueType type);

    List<Device> getDevicesByProto(SourceProtocol proto);

    List<Device> getDevices();

    Device getDevice(SourceProtocol protocol, String channel);

    DeviceValue getDeviceValue(SourceProtocol protocol, String channel, String value);

    List getHistory(SourceProtocol proto, String channel, String label, Date start);

    List getHistory(SourceProtocol proto, String channel, String label, Date start, Date stop);

    void deleteHistory(SourceProtocol proto, String channel, String label, Date start);
}
