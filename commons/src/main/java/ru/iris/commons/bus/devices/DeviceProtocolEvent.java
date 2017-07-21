package ru.iris.commons.bus.devices;

import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

/**
 * Created by nix on 03.11.16.
 */
public class DeviceProtocolEvent extends AbstractDeviceEvent {

    public DeviceProtocolEvent(Short channel, SourceProtocol protocol, String label) {
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

    @Override
    public String toString() {
        return "DeviceProtocolEvent{" +
                "channel=" + channel +
                ", protocol=" + protocol +
                ", label='" + label + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", valueType=" + valueType +
                '}';
    }

}
