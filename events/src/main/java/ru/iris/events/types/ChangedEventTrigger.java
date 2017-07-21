package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

public class ChangedEventTrigger implements EventTrigger {
    private String itemName;
    private Object from;
    private Object to;

    public ChangedEventTrigger(String itemName, Object from, Object to) {
        this.itemName = itemName;
        this.from = from;
        this.to = to;
    }

    public ChangedEventTrigger(String itemName) {
        this.itemName = itemName;
        this.from = null;
        this.to = null;
    }

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return type == TriggerType.CHANGE && device.getHumanReadableName().equals(itemName);
    }

    @Override
    public String getItem() {
        return this.itemName;
    }

}
