package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class NooliteValueRemoved extends AbstractEvent {

	private short node;
	private String label;
	private Object value;

	public NooliteValueRemoved(short node, String label, Object value) {
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
		return "NooliteValueRemoved{" +
				"node=" + node +
				", label='" + label + '\'' +
				", value=" + value +
				'}';
	}
}

