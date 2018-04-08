package ru.iris.models.protocol.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.bus.service.ServiceEvent;
import ru.iris.models.bus.speak.SpeakEvent;
import ru.iris.models.protocol.enums.ValueType;

/**
 * @author Nikolay Viguro, 25.08.17
 */

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "classtype")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DataLevel.class, name = "data-level"),
		@JsonSubTypes.Type(value = DataId.class, name = "data-id"),
		@JsonSubTypes.Type(value = DataSubChannelLevel.class, name = "data-subchannel")
})
public abstract class EventData {
}
