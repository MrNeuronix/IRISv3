package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteDeviceBatteryLow extends AbstractEvent {

	private byte channel;
	private Notification notification;

	public NooliteDeviceBatteryLow(byte channel) {
		this.channel = channel;
	}

	public NooliteDeviceBatteryLow(byte channel, Notification notification) {
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
		return "NooliteDeviceBatteryLow{" +
				"channel=" + channel +
				", notification=" + notification +
				'}';
	}
}

