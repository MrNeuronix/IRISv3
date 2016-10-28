package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.noolite4j.watchers.Notification;

public class NooliteDeviceAdded extends AbstractEvent {

	private Notification notification;

	public NooliteDeviceAdded(Notification notification) {
		this.notification = notification;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	@Override
	public String toString() {
		return "NooliteDeviceAdded{" +
				"notification=" + notification +
				'}';
	}
}

