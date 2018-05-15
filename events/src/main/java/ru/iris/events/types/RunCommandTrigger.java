package ru.iris.events.types;

import lombok.Getter;

@Getter
public class RunCommandTrigger implements EventTrigger {
    private String item;

    public RunCommandTrigger(String itemName) {
        this.item = itemName;
    }

    @Override
    public boolean evaluate(String commandname, TriggerType type) {
        return (type == TriggerType.RUN && this.item.equals(commandname));
    }
}
