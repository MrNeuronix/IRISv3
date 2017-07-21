package ru.iris.events.types;

import java.util.List;

public interface Rule {

    List<EventTrigger> getEventTrigger();

    void execute(Event event);

}
