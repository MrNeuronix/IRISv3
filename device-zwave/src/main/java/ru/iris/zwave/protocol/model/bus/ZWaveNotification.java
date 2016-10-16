package ru.iris.zwave.protocol.model.bus;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveNotification extends AbstractEvent {

	private short node;

	public ZWaveNotification(short node) {
		this.node = node;
	}

	public short getNode() {
		return node;
	}

	public void setNode(short node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "ZWaveNotification{" +
				"node=" + node +
				'}';
	}
}
