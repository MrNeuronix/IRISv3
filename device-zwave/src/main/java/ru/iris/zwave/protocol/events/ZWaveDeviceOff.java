package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDeviceOff extends AbstractEvent {

	private short node;

	public ZWaveDeviceOff(short node) {
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
		return "ZWaveDeviceOff{" +
				"node=" + node +
				'}';
	}
}
