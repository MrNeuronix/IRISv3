package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceOff extends AbstractEvent {

	private Short channel;

	public DeviceOff(Short channel) {
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
		return "DeviceOff{" +
				"channel=" + channel +
				'}';
	}
}

