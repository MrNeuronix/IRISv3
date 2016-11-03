package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

/**
 * ShutdownTrigger to allow a Rule to listen for shutdown event
 * 
 * @author Simon Merschjohann
 */
public class ShutdownTrigger implements EventTrigger {

	@Override
	public boolean evaluate(Device device, TriggerType type) {
		return type == TriggerType.SHUTDOWN;
	}

	@Override
	public String getItem() {
		return null;
	}

}
