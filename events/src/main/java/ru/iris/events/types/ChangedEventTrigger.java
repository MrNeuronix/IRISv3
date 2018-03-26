package ru.iris.events.types;

import ru.iris.models.database.Device;

public class ChangedEventTrigger implements EventTrigger {
    private String itemName;
    private Object from;
    private Object to;

    public ChangedEventTrigger(String itemName, Object from, Object to) {
        this.itemName = itemName;
        this.from = from;
        this.to = to;
    }

    public ChangedEventTrigger(String itemName) {
        this.itemName = itemName;
        this.from = null;
        this.to = null;
    }

    @Override
    public boolean evaluate(Device device, TriggerType type) {
    	  String ident = device.getSource().toString().toLowerCase() + "/channel/" + device.getChannel();
        return type == TriggerType.CHANGE && ident.equals(itemName);
    }

    @Override
    public String getItem() {
        return this.itemName;
    }

}
