package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDeviceOn extends AbstractEvent {

	private Byte node;

	public ZWaveDeviceOn(Byte node) {
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
		return "ZWaveDeviceOn{" +
				"node=" + node +
				'}';
	}
}
