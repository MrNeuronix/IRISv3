package ru.iris.models.bus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author nix (31.10.2017)
 */

@AllArgsConstructor
@ToString
public enum Queue {

	EVENT_DEVICE("event.device"),
	EVENT_STATE("event.device.state"),
	EVENT_TRIGGER("event.device.trigger"),

	EVENT_DEVICE_CONNECTED("event.device.connected"),
	EVENT_DEVICE_DISCONNECTED("event.device.disconnected"),

	EVENT_DEVICE_ADDED("event.device.added"),

	EVENT_DEVICE_OFF("event.device.off"),
	EVENT_DEVICE_ON("event.device.on"),
	EVENT_DEVICE_DIM("event.device.dim"),
	EVENT_DEVICE_BRIGHT("event.device.bright"),
	EVENT_DEVICE_LEVEL("event.device.level"),
	EVENT_DEVICE_STOPDIMBRIGHT("event.device.stopdimbright"),

	EVENT_TEMPERATURE("event.device.temperature"),
	EVENT_HUMIDITY("event.device.humidity"),

	EVENT_VOLTAGE("event.device.voltage"),
	EVENT_BATTERY_STATUS("event.device.battery"),

	EVENT_GPS_DATA("event.device.gps"),
	EVENT_TRANSPORT("event.transport"),

	EVENT_BUTTON(""),

	COMMAND_DEVICE("command.device"),
	UNKNOWN("unknown");

	@Getter
	private final String string;
}
