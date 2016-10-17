package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.abstracts.AbstractDevice;

import java.util.HashMap;
import java.util.Map;

public class NooliteDevice extends AbstractDevice {

	private Map<String, NooliteDeviceValue> values = new HashMap<>();

	@Override
	public Map<String, NooliteDeviceValue> getDeviceValues() {
		return values;
	}

	@Override
	public void setDeviceValues(Map<String, ? extends DeviceValue> values) {
		this.values = (Map<String, NooliteDeviceValue>) values;
	}

	@Override
	public String toString() {
		return "NooliteDevice{" +
				"id=" + id +
				", date=" + date +
				", humanReadable='" + humanReadable + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", productName='" + productName + '\'' +
				", node=" + node +
				", source=" + source +
				", type=" + type +
				", zone=" + zone +
				", state=" + state +
				", values=" + values +
				'}';
	}

}
