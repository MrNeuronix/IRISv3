package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;
import ru.iris.commons.protocol.enums.ValueType;

public class NooliteDeviceValue extends AbstractDeviceValue<NooliteDeviceValueChange> {

	public NooliteDeviceValue() {
	}

	public NooliteDeviceValue(String name, byte value, ValueType type) {
		super.name = name;
		super.currentValue = value;
		super.type = type;
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
