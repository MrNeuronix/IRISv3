package ru.iris.events.types;

import ru.iris.models.database.Device;

public interface EventTrigger {
    String getItem();

    default boolean evaluate(Device device, TriggerType type) {
        return false;
    }

    default boolean evaluate(String param, TriggerType type) {
        return false;
    }
}
