package ru.iris.events.types;

import ru.iris.models.database.Device;

public interface EventTrigger {

    String getItem();
    boolean evaluate(Device device, TriggerType type);

}
