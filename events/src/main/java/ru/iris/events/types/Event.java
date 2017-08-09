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

    public Event(TriggerType triggerType, Device device) {
        this.triggerType = triggerType;
        this.device = device;
    }
}
