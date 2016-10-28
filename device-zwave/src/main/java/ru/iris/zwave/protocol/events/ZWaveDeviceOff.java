package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDeviceOff extends AbstractEvent {

	private Short node;

	public ZWaveDeviceOff(Short node) {
		this.node = node;
	}

	public Short getNode() {
		return node;
	}

	public void setNode(Short node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "ZWaveDeviceOff{" +
				"node=" + node +
				'}';
	}
}
