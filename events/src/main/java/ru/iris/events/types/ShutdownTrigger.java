package ru.iris.events.types;

import ru.iris.commons.database.model.Device;

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
