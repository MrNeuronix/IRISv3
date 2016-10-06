package ru.iris.commons.bus.models.protocol.zwave;

import ru.iris.commons.bus.AbstractEvent;

public class ZWavePollingDisabled extends AbstractEvent {

	private short node;

	public ZWavePollingDisabled(short node) {
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
		return "ZWavePollingDisabled{" +
				"node=" + node +
				'}';
	}
}
