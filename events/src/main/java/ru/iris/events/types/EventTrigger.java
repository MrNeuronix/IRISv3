package ru.iris.events.types;

import ru.iris.commons.database.model.Device;

public interface EventTrigger {

    String getItem();
    boolean evaluate(Device device, TriggerType type);

}
