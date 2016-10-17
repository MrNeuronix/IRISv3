package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteUnbindRXChannel extends AbstractEvent {

	private byte channel;

	public NooliteUnbindRXChannel(byte channel) {
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

