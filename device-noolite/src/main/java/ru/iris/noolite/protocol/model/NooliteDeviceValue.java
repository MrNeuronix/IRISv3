package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class NooliteDeviceValue extends AbstractDeviceValue<NooliteDeviceValueChange> {

	public NooliteDeviceValue() {
	}

	public NooliteDeviceValue(String name, byte value) {
		super.name = name;
		super.currentValue = value;
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
