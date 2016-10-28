package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteDeviceOn extends AbstractEvent {

	private byte channel;
	private Notification notification;

	public NooliteDeviceOn(byte channel) {
		this.channel = channel;
	}

	public NooliteDeviceOn(byte channel, Notification notification) {
		this.channel = channel;
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

	@Override
	public String toString() {
		return "NooliteDeviceOn{" +
				"channel=" + channel +
				", notification=" + notification +
				'}';
	}
}

