package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteUnbindRXChannel extends AbstractEvent {

	private Short channel;

	public NooliteUnbindRXChannel(Short channel) {
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
		return "NooliteBindRXChannel{" +
				"channel=" + channel +
				'}';
	}
}

