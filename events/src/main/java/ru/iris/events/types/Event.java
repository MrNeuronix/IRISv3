package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

public class Event {

	private TriggerType triggerType;
	private Device device;

	public Event(TriggerType triggerType, Device device) {
		this.triggerType = triggerType;
		this.device = device;
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public String toString() {
		return "Event{" +
				"triggerType=" + triggerType +
				", device=" + device +
				'}';
	}
}
