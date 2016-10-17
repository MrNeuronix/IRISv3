package ru.iris.zwave.protocol.events;

import ru.iris.commons.bus.AbstractEvent;

public class ZWaveDriverReady extends AbstractEvent {

	private long homeId;

	public ZWaveDriverReady(long homeId) {
		this.homeId = homeId;
	}

	public long getHomeId() {
		return homeId;
	}

	public void setHomeId(long homeId) {
		this.homeId = homeId;
	}

	@Override
	public String toString() {
		return "ZWaveDriverReady{" +
				"homeId=" + homeId +
				'}';
	}
}
