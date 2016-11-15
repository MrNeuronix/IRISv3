package ru.iris.noolite.protocol.model;

import ru.iris.commons.LIFO;
import ru.iris.commons.protocol.DeviceValueChange;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;
import ru.iris.commons.protocol.enums.ValueType;

public class NooliteDeviceValue extends AbstractDeviceValue {

	private LIFO<NooliteDeviceValueChange> changes = new LIFO<>(5);

	public NooliteDeviceValue() {
	}

	public NooliteDeviceValue(String name, Short value, ValueType type) {
		super.name = name;
		super.currentValue = value;
		super.type = type;
	}

	@Override
	public LIFO<NooliteDeviceValueChange> getChanges() {
		return changes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setChanges(LIFO<? extends DeviceValueChange> changes) {
		this.changes = (LIFO<NooliteDeviceValueChange>) changes;
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
