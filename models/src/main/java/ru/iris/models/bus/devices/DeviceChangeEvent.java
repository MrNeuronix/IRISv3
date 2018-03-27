package ru.iris.models.bus.devices;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.data.DataSubChannelLevel;
import ru.iris.models.protocol.data.EventData;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.ValueType;

@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class DeviceChangeEvent extends AbstractDeviceEvent {

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, String from, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataLevel(from, to, valueType);
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataLevel(to, valueType);
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, Integer subchannel, String to) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataSubChannelLevel(subchannel, to, ValueType.UNKNOWN);
    }

    public DeviceChangeEvent(String channel, SourceProtocol protocol, String label, Integer subchannel, String to, ValueType valueType) {
        this.channel = channel;
        this.protocol = protocol;
        this.eventLabel = label;
        this.data = new DataSubChannelLevel(subchannel, to, valueType);
    }

		@Builder
		public DeviceChangeEvent(String channel, SourceProtocol protocol, String eventLabel, EventData data) {
			super();
			super.channel = channel;
			super.protocol = protocol;
			super.eventLabel = eventLabel;
			super.data = data;
		}
}
