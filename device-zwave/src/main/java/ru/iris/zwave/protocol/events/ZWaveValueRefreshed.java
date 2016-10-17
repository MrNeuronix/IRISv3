package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveValueRefreshed extends AbstractEvent {

	private short node;

	public ZWaveValueRefreshed(short node) {
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
		return "ZWaveValueRefreshed{" +
				"node=" + node +
				'}';
	}
}
