package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.abstracts.AbstractDevice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NooliteDevice extends AbstractDevice {

	private Map<String, NooliteDeviceValue> values = new ConcurrentHashMap<>();

	@Override
	public Map<String, NooliteDeviceValue> getDeviceValues() {
		return values;
	}

	@Override
	@SuppressWarnings("unchecked")
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
				", channel=" + channel +
				", source=" + source +
				", type=" + type +
				", zone=" + zone +
				", state=" + state +
				", values=" + values +
				'}';
	}
}
