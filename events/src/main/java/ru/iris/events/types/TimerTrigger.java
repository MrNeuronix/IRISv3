package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

public class TimerTrigger implements EventTrigger {

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

    public String getCron() {
        return this.cron;
    }
}
