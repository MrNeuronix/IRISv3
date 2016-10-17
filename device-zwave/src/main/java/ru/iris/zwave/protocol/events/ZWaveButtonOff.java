package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveButtonOff extends AbstractEvent {

	private short node;

	public ZWaveButtonOff(short node) {
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
		return "ZWaveButtonOff{" +
				"node=" + node +
				'}';
	}
}
