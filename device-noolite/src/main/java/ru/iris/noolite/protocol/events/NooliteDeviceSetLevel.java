package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteDeviceSetLevel extends AbstractEvent {

	private Short channel;
	private Short level;

	public NooliteDeviceSetLevel(Short channel, Short level) {
		this.channel = channel;
		this.level = level;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}

	public Short getLevel() {
		return level;
	}

	public void setLevel(Short level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "NooliteDeviceSetLevel{" +
				"channel=" + channel +
				", level=" + level +
				'}';
	}
}

