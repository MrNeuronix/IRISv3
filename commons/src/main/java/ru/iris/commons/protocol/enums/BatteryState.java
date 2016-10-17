package ru.iris.commons.protocol.enums;

public enum BatteryState {

	OK("ok"),
	LOW("low"),
	UNKNOWN("unknown");

	private final String name;

	BatteryState(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return otherName != null && name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}
