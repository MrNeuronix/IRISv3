package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceCancelRequest extends AbstractEvent {

	private Short node;

	public DeviceCancelRequest(Short node) {
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
		return "DeviceCancelRequest{" +
				"node=" + node +
				'}';
	}
}
