package ru.iris.commons.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.commons.protocol.data.DataLevel;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode(callSuper = false)
public class DeviceProtocolEvent extends AbstractDeviceEvent {

    public DeviceProtocolEvent(String channel, SourceProtocol protocol, String label) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
    }

    public DeviceProtocolEvent(SourceProtocol protocol, String label) {
        this.protocol = protocol;
        this.eventLabel = label;
    }

    public DeviceProtocolEvent(SourceProtocol protocol, String label, String value, ValueType type) {
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataLevel(value, type);
        this.clazz = DataLevel.class;
    }

}
