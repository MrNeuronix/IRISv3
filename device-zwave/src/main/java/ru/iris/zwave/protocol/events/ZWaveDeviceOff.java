package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDeviceOff extends AbstractEvent {

	private Byte node;

	public ZWaveDeviceOff(Byte node) {
		this.node = node;
	}

	public Byte getNode() {
		return node;
	}

	public void setNode(Byte node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "ZWaveDeviceOff{" +
				"node=" + node +
				'}';
	}
}
