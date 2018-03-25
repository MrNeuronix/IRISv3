package ru.iris.events.types;

import lombok.Getter;
import ru.iris.models.database.Device;

@Getter
public class CommandEventTrigger implements EventTrigger {

    private String item;

    public CommandEventTrigger(String itemName) {
        this.item = itemName;
    }

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return (type == TriggerType.COMMAND && this.item.equals(device.getHumanReadable()));
    }
}
