package ru.iris.commons.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode
public class DeviceCommandEvent extends AbstractDeviceEvent {

    public DeviceCommandEvent(Short channel, SourceProtocol protocol, String label, Object to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
        this.to = to;
        this.valueType = valueType;
    }

    public DeviceCommandEvent(Short channel, SourceProtocol protocol, String label, Object to) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
        this.to = to;
    }

    public DeviceCommandEvent(Short channel, SourceProtocol protocol, String label) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
    }
}
