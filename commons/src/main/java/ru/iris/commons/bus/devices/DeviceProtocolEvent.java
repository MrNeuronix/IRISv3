package ru.iris.commons.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode
public class DeviceProtocolEvent extends AbstractDeviceEvent {

    public DeviceProtocolEvent(String channel, SourceProtocol protocol, String label) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
    }

    public DeviceProtocolEvent(SourceProtocol protocol, String label) {
        this.protocol = protocol;
        this.label = label;
    }

    public DeviceProtocolEvent(SourceProtocol protocol, String label, Object value, ValueType type) {
        this.protocol = protocol;
        this.label = label;
        this.to = value;
        this.valueType = type;
    }

}
