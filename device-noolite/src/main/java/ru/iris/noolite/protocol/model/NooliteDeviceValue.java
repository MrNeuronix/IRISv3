package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class NooliteDeviceValue extends AbstractDeviceValue {

	@Override
	public String toString() {
		return "NooliteDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + value +
				", units='" + units + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", additionalData='" + additionalData + '\'' +
				'}';
	}

}
