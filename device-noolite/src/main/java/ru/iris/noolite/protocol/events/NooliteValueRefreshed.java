package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteValueRefreshed extends AbstractEvent {

	private short node;

	public NooliteValueRefreshed(short node) {
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
		return "NooliteValueRefreshed{" +
				"node=" + node +
				'}';
	}
}
