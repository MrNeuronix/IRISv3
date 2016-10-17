package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteDeviceSetLevel extends AbstractEvent {

	private byte channel;
	private byte level;

	public NooliteDeviceSetLevel(byte channel, byte level) {
		this.channel = channel;
		this.level = level;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public byte getLevel() {
		return level;
	}

	public void setLevel(byte level) {
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

