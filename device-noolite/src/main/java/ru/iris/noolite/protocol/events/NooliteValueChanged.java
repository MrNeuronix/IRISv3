package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteValueChanged extends AbstractEvent {

	private byte channel;
	private byte level;
	private Notification notification;

	public NooliteValueChanged(byte channel, byte level) {
		this.channel = channel;
		this.level = level;
	}

	public NooliteValueChanged(byte channel, byte level, Notification notification) {
		this.channel = channel;
		this.level = level;
		this.notification = notification;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
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
		return "NooliteValueChangedl{" +
				"channel=" + channel +
				", level=" + level +
				", notification=" + notification +
				'}';
	}
}

