package ru.iris.events.types;

import java.util.List;

/**
 * Rule-Interface: A script can implement multiple Rules
 * 
 * @author Simon Merschjohann
 * @since 1.7.0
 */
public interface Rule {
	List<EventTrigger> getEventTrigger();
	void execute(Event event);
}
