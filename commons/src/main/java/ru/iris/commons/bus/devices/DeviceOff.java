package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceOff extends AbstractEvent {

	private byte channel;

	public DeviceOff(byte channel) {
		this.channel = channel;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		return "DeviceOff{" +
				"channel=" + channel +
				'}';
	}
}

