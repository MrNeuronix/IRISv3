package ru.iris.events.types;

import ru.iris.models.database.Device;

public class StartupTrigger implements EventTrigger {

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return type == TriggerType.STARTUP;
    }

    @Override
    public String getItem() {
        return null;
    }
}
