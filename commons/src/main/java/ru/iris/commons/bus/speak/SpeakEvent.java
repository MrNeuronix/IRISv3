package ru.iris.commons.bus.speak;

import ru.iris.commons.bus.devices.AbstractDeviceEvent;
import ru.iris.commons.database.model.Zone;

public class SpeakEvent extends AbstractDeviceEvent {

    private String text;
    private Zone zone;

    public SpeakEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "SpeakEvent{" +
                "text='" + text + '\'' +
                ", zone=" + zone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeakEvent)) return false;

        SpeakEvent that = (SpeakEvent) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return zone != null ? zone.equals(that.zone) : that.zone == null;

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        return result;
    }
}
