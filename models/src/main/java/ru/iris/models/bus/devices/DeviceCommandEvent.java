package ru.iris.models.bus.devices;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.data.DataSubChannelLevel;
import ru.iris.models.protocol.enums.EventLabel;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

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

    public DeviceCommandEvent(String channel, Integer subchannel, SourceProtocol protocol, EventLabel eventLabel) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataSubChannelLevel(subchannel, ValueType.UNKNOWN);
        this.clazz = DataSubChannelLevel.class;
    }

    public DeviceCommandEvent(String channel, Integer subchannel, SourceProtocol protocol, EventLabel eventLabel, String to) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = eventLabel.getName();
        this.data = new DataSubChannelLevel(subchannel, to, ValueType.UNKNOWN);
        this.clazz = DataSubChannelLevel.class;
    }

    @Builder
		public DeviceCommandEvent(String channel, SourceProtocol protocol, String eventLabel, Object data, Class clazz) {
				super();
				super.channel = channel;
				super.protocol = protocol;
				super.eventLabel = eventLabel;
				super.data = data;
				super.clazz = clazz;
		}
}
