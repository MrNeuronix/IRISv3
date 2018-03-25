package ru.iris.events.types;

import lombok.Getter;
import ru.iris.models.database.Device;

public class TimerTrigger implements EventTrigger {

    @Getter
    private String cron;

    public TimerTrigger(String cron) {
        this.cron = cron;
    }

    @Override
    public String getItem() {
        return null;
    }

    @Override
    public boolean evaluate(Device device, TriggerType type) {
        return type == TriggerType.TIMER;
    }
}
