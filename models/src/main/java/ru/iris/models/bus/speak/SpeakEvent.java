package ru.iris.models.bus.speak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.iris.models.bus.devices.AbstractDeviceEvent;
import ru.iris.models.database.Zone;

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
