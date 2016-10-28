package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceOn extends AbstractEvent {

	private Short channel;

	public DeviceOn(Short channel) {
		this.channel = channel;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		return "DeviceOn{" +
				"channel=" + channel +
				'}';
	}
}

