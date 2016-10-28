package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteDeviceOff extends AbstractEvent {

	private byte channel;
	private Notification notification;

	public NooliteDeviceOff(byte channel) {
		this.channel = channel;
	}

	public NooliteDeviceOff(byte channel, Notification notification) {
		this.channel = channel;
		this.notification = notification;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	@Override
	public String toString() {
		return "NooliteDeviceOff{" +
				"channel=" + channel +
				", notification=" + notification +
				'}';
	}
}

