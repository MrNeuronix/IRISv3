package ru.iris.commons.bus.models.protocol.zwave;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveNodeEvent extends AbstractEvent {

	private short node;

	public ZWaveNodeEvent(short node) {
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
		return "ZWaveNodeEvent{" +
				"node=" + node +
				'}';
	}
}
