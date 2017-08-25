package ru.iris.commons.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.iris.commons.protocol.data.DataLevel;
import ru.iris.commons.protocol.data.DataSubChannelLevel;
import ru.iris.commons.protocol.enums.EventLabel;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode(callSuper = false)
public class DeviceCommandEvent extends AbstractDeviceEvent {

    public DeviceCommandEvent(String channel, SourceProtocol protocol, EventLabel eventLabel, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataLevel(to, valueType);
        this.clazz = DataLevel.class;
    }

    public DeviceCommandEvent(String channel, SourceProtocol protocol, EventLabel eventLabel, String to) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataLevel(to, ValueType.UNKNOWN);
        this.clazz = DataLevel.class;
    }

    public DeviceCommandEvent(String channel, SourceProtocol protocol, EventLabel eventLabel) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
    }

    public DeviceCommandEvent(String channel, SourceProtocol protocol, EventLabel eventLabel, Integer subchannel) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataSubChannelLevel(subchannel, ValueType.UNKNOWN);
        this.clazz = DataSubChannelLevel.class;
    }

    public DeviceCommandEvent(String channel, SourceProtocol protocol, EventLabel eventLabel, Integer subchannel, String to) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataSubChannelLevel(subchannel, to, ValueType.UNKNOWN);
        this.clazz = DataSubChannelLevel.class;
    }
}
