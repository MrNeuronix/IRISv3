package ru.iris.models.bus.devices;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.data.DataSubChannelLevel;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode(callSuper = false)
public class DeviceChangeEvent extends AbstractDeviceEvent {

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, String from, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataLevel(from, to, valueType);
        this.clazz = DataLevel.class;
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataLevel(to, valueType);
        this.clazz = DataLevel.class;
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, Integer subchannel, String to) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataSubChannelLevel(subchannel, to, ValueType.UNKNOWN);
        this.clazz = DataSubChannelLevel.class;
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, Integer subchannel, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataSubChannelLevel(subchannel, to, valueType);
        this.clazz = DataSubChannelLevel.class;
    }
}
