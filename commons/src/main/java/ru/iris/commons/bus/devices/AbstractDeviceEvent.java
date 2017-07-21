package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.Event;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

public abstract class AbstractDeviceEvent implements Event {

    protected Short channel;
    protected SourceProtocol protocol;
    protected String label;
    protected Object from;
    protected Object to;
    protected ValueType valueType = ValueType.UNKNOWN;

    public Short getChannel() {
        return channel;
    }

    public void setChannel(Short channel) {
        this.channel = channel;
    }

    public SourceProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(SourceProtocol protocol) {
        this.protocol = protocol;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(Object from) {
        this.from = from;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(Object to) {
        this.to = to;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        return "AbstractDeviceEvent{" +
                "channel=" + channel +
                ", protocol=" + protocol +
                ", label='" + label + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", valueType=" + valueType +
                '}';
    }
}
