package ru.iris.zwave.protocol;

import ru.iris.commons.protocol.abstracts.AbstractDevice;

public class ZWaveDevice extends AbstractDevice {

	@Override
	public String toString() {
		return "ZWaveDevice{" +
				"id=" + id +
				", date=" + date +
				", internalName='" + internalName + '\'' +
				", humanReadable='" + humanReadable + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", productName='" + productName + '\'' +
				", source=" + source +
				", type=" + type +
				", zone=" + zone +
				", state=" + state +
				", values=" + values +
				'}';
	}

}
