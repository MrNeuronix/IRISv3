package ru.iris.events.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.iris.commons.database.model.Device;

@ToString
@Getter
@Setter
public class Event {

    private TriggerType triggerType;
    private Device device;
    private String topic;

    public Event(TriggerType triggerType, Device device, String topic) {
        this.triggerType = triggerType;
        this.device = device;
        this.topic = topic;
    }
}
