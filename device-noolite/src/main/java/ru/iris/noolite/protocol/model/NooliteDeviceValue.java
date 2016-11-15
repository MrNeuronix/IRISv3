package ru.iris.noolite.protocol.model;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import ru.iris.commons.protocol.DeviceValueChange;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;
import ru.iris.commons.protocol.enums.ValueType;

public class NooliteDeviceValue extends AbstractDeviceValue {

	private Deque<NooliteDeviceValueChange> changes = new ConcurrentLinkedDeque<>();

	public NooliteDeviceValue() {
	}

	public NooliteDeviceValue(String name, Short value, ValueType type) {
		super.name = name;
		super.currentValue = value;
		super.type = type;
	}

	@Override
	public Deque<NooliteDeviceValueChange> getChanges() {
		return changes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setChanges(Deque<? extends DeviceValueChange> changes) {
		this.changes = (Deque<NooliteDeviceValueChange>) changes;
	}

	@Override
	public String toString() {
		return "NooliteDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", currentValue=" + currentValue +
				", units='" + units + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", additionalData='" + additionalData + '\'' +
				'}';
	}

}
