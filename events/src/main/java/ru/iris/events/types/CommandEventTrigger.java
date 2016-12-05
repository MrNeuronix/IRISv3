package ru.iris.events.types;

import ru.iris.commons.protocol.Device;

public class CommandEventTrigger implements EventTrigger {

	private String itemName;

	public CommandEventTrigger(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public boolean evaluate(Device device, TriggerType type) {
		return (type == TriggerType.COMMAND && this.itemName.equals(device.getHumanReadableName()));
	}

	@Override
	public String getItem() {
		return this.itemName;
	}

}
