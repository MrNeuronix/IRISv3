package ru.iris.events.types;

import ru.iris.models.database.Device;

public class ShutdownTrigger implements EventTrigger {

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return type == TriggerType.SHUTDOWN;
    }

    @Override
    public String getItem() {
        return null;
    }

}
