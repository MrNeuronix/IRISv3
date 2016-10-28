package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteDeviceStopDimBright extends AbstractEvent {

	private byte channel;
	private Notification notification;

	public NooliteDeviceStopDimBright(byte channel) {
		this.channel = channel;
	}

	public NooliteDeviceStopDimBright(byte channel, Notification notification) {
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
		return "NooliteDeviceStopDimBright{" +
				"channel=" + channel +
				", notification=" + notification +
				'}';
	}
}

