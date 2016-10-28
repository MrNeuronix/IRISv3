package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceAdd extends AbstractEvent {

	private Short node;

	public Short getNode() {
		return node;
	}

	public void setNode(Short node) {
		this.node = node;
	}

	public DeviceAdd(Short node) {

		this.node = node;
	}

	@Override
	public String toString() {
		return "DeviceAddRequest{}";
	}
}
