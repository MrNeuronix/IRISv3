package ru.iris.commons.bus.devices;

import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

/**
 * Created by nix on 03.11.16.
 */
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

	@Override
	public String toString() {
		return "DeviceChangedEvent{" +
				"channel=" + channel +
				", protocol=" + protocol +
				", label='" + label + '\'' +
				", from=" + from +
				", to=" + to +
				", valueType=" + valueType +
				'}';
	}
}
