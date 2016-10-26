package ru.iris.zwave.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDevice;

import java.util.HashMap;
import java.util.Map;

public class ZWaveDevice extends AbstractDevice<ZWaveDeviceValue> {

	private long homeId;
	private Map<String, ZWaveDeviceValue> values = new HashMap<>();

	public long getHomeId() {
		return homeId;
	}

	public void setHomeId(long homeId) {
		this.homeId = homeId;
	}

	@Override
	public Map<String, ZWaveDeviceValue> getDeviceValues() {
		return values;
	}

	public void setDeviceValues(Map<String, ZWaveDeviceValue> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "ZWaveDevice{" +
				"id=" + id +
				", date=" + date +
				", humanReadable='" + humanReadable + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", productName='" + productName + '\'' +
				", homeId=" + homeId +
				", node=" + node +
				", source=" + source +
				", type=" + type +
				", zone=" + zone +
				", state=" + state +
				", values=" + values +
				'}';
	}

}
