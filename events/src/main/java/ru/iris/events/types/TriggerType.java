package ru.iris.events.types;

public enum TriggerType {
    CHANGE,
    COMMAND,
    RUN,
    STARTUP,
    SHUTDOWN,
    TIMER;

    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }
}
