package ru.iris.zwave.protocol;

import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class ZWaveDeviceValue extends AbstractDeviceValue {

	@Override
	public String toString() {
		return "ZWaveDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + value +
				'}';
	}

}
