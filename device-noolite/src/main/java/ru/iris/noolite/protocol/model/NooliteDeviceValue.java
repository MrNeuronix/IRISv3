package ru.iris.noolite.protocol.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;
import ru.iris.commons.protocol.enums.ValueType;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"changes"})
public class NooliteDeviceValue extends AbstractDeviceValue<NooliteDeviceValueChange> {

	public NooliteDeviceValue() {
	}

	public NooliteDeviceValue(String name, Short value, ValueType type) {
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
