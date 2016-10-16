package ru.iris.commons.protocol.enums;

public enum DeviceType {

	CONTROLLER("controller"),
	BINARY_SWITCH("switch"),
	MULTILEVEL_SWITCH("dimmer"),
	ALARM_SENSOR("alarmsensor"),
	BINARY_SENSOR("binarysensor"),
	MULTILEVEL_SENSOR("binarysensor"),
	SIMPLE_METER("simplemeter"),
	TEMP_HUMI_SENSOR("temphumisensor"),
	DOOR_SENSOR("doorsensor"),
	DRAPES("drapes"),
	THERMOSTAT("thermostat"),
	MOTION_SENSOR("motionsensor");

	private final String name;

	DeviceType(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return otherName != null && name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}
