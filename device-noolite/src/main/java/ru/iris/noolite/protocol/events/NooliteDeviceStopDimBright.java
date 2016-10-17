package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteDeviceStopDimBright extends AbstractEvent {

	private byte channel;

	public NooliteDeviceStopDimBright(byte channel) {
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
		return "NooliteDeviceStopDimBright{" +
				"channel=" + channel +
				'}';
	}
}

