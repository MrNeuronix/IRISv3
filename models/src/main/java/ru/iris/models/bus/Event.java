package ru.iris.models.bus;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.bus.service.ServiceEvent;
import ru.iris.models.bus.speak.SpeakEvent;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DeviceChangeEvent.class, name = "device-change"),
		@JsonSubTypes.Type(value = DeviceCommandEvent.class, name = "device-command"),
		@JsonSubTypes.Type(value = DeviceProtocolEvent.class, name = "device-protocol"),
		@JsonSubTypes.Type(value = DeviceCommandEvent.class, name = "device-command"),
		@JsonSubTypes.Type(value = ServiceEvent.class, name = "service"),
		@JsonSubTypes.Type(value = SpeakEvent.class, name = "speak")
})
public abstract class Event {
}
