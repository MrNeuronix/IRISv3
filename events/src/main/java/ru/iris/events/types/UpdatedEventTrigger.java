package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

/**
 * UpdatedEventTrigger is used by a Script to listen for item updates
 * 
 * @author Steve Bate
 */
public class UpdatedEventTrigger implements EventTrigger {
	private String itemName;

	public UpdatedEventTrigger(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public boolean evaluate(Device device, TriggerType type) {
		return type == TriggerType.UPDATE && device.getHumanReadableName().equals(itemName);
	}

	@Override
	public String getItem() {
		return this.itemName;
	}
}
