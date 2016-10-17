package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteBindRXChannel extends AbstractEvent {

	private byte channel;

	public NooliteBindRXChannel(byte channel) {
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
		return "NooliteBindRXChannel{" +
				"channel=" + channel +
				'}';
	}
}

