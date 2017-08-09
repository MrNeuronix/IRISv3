package ru.iris.commons.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode
public class DeviceChangeEvent extends AbstractDeviceEvent {

    public DeviceChangeEvent(Short channel, SourceProtocol protocol, String label, Object from, Object to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
        this.from = from;
        this.to = to;
        this.valueType = valueType;
    }

    public DeviceChangeEvent(Short channel, SourceProtocol protocol, String label, Object to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.label = label;
        this.to = to;
        this.valueType = valueType;
    }
}
