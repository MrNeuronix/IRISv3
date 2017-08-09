package ru.iris.commons.bus.speak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.iris.commons.bus.devices.AbstractDeviceEvent;
import ru.iris.commons.database.model.Zone;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class SpeakEvent extends AbstractDeviceEvent {

    private String text;
    private Zone zone;

    public SpeakEvent(String text) {
        this.text = text;
    }
}
