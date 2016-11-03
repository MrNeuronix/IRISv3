package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

/**
 * StartupTrigger to allow a Rule to listen for startup event
 * 
 * @author Simon Merschjohann
 */
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
