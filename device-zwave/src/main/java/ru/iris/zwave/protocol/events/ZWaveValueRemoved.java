package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveValueRemoved extends AbstractEvent {

	private short node;
	private String label;
	private Object value;

	public ZWaveValueRemoved(short node, String label, Object value) {
		this.node = node;
		this.label = label;
		this.value = value;
	}

	public short getNode() {
		return node;
	}

	public void setNode(short node) {
		this.node = node;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ZWaveValueRemoved{" +
				"node=" + node +
				", label='" + label + '\'' +
				", value=" + value +
				'}';
	}
}
