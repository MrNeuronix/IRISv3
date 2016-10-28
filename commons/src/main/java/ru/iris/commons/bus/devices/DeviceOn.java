package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceOn extends AbstractEvent {

	private byte channel;

	public DeviceOn(byte channel) {
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
		return "DeviceOn{" +
				"channel=" + channel +
				'}';
	}
}

