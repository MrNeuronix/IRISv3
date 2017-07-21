package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

public class StartupTrigger implements EventTrigger {
    public StartupTrigger() {

    }

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return type == TriggerType.STARTUP;
    }

    @Override
    public String getItem() {
        return null;
    }
}
