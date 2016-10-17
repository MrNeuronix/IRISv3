package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteDeviceBatteryLow extends AbstractEvent {

	private byte channel;

	public NooliteDeviceBatteryLow(byte channel) {
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
		return "NooliteDeviceBatteryLow{" +
				'}';
	}
}

