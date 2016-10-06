package ru.iris.zwave.protocol.model;

import ru.iris.commons.protocol.abstracts.AbstractDevice;

public class ZWaveDevice extends AbstractDevice {

	private long homeId;
	private short node;

	public long getHomeId() {
		return homeId;
	}

	public void setHomeId(long homeId) {
		this.homeId = homeId;
	}

	public short getNode() {
		return node;
	}

	public void setNode(short node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "ZWaveDevice{" +
				"id=" + id +
				", date=" + date +
				", internalName='" + internalName + '\'' +
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
