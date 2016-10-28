package ru.iris.commons.bus.devices;

import ru.iris.commons.bus.AbstractEvent;

public class DeviceSetValue extends AbstractEvent {

	private byte channel;
	private String name;
	private Object value;

	public DeviceSetValue(byte channel, String name, Object value) {
		this.channel = channel;
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return "DeviceSetValue{" +
				"channel=" + channel +
				", name='" + name + '\'' +
				", value=" + value +
				'}';
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}

