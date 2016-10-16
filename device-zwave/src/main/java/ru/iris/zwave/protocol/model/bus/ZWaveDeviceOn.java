package ru.iris.zwave.protocol.model.bus;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDeviceOn extends AbstractEvent {

	private short node;

	public ZWaveDeviceOn(short node) {
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
		return "ZWaveDeviceOn{" +
				"node=" + node +
				'}';
	}
}
