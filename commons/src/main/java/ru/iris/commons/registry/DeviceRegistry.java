package ru.iris.commons.registry;

import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValue;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;
import java.util.List;

/**
 * @author Nikolay Viguro, 10.08.17
 */
public interface DeviceRegistry {

    Device addOrUpdateDevice(Device device);

    void addOrUpdateDevices(List<Device> devices);

    DeviceValue addChange(DeviceValue value);

    DeviceValue addChange(Device device, String key, String level, ValueType type);

    List<Device> getDevicesByProto(SourceProtocol proto);

    List<Device> getDevices();

    Device getDevice(SourceProtocol protocol, Short channel);

    Device getDevice(String humanReadableIdent);

    DeviceValue getDeviceValue(SourceProtocol protocol, Short channel, String value);

    DeviceValue getDeviceValue(String humanReadableIdent, String value);

    List getHistory(String humanReadableIdent, String label, Date start);

    List getHistory(String humanReadableIdent, String label, Date start, Date stop);

    List getHistory(SourceProtocol proto, Short channel, String label, Date start);

    List getHistory(SourceProtocol proto, Short channel, String label, Date start, Date stop);
}
