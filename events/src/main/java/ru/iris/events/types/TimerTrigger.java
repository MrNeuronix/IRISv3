package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

/**
 * TimerTrigger to allow a Rule to be called on a periodic (cron) basis
 * 
 * @author Simon Merschjohann
 * @since 1.7.0
 */
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
