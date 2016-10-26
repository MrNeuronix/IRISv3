package ru.iris.noolite.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDevice;

public class NooliteDevice extends AbstractDevice<NooliteDeviceValue> {

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
