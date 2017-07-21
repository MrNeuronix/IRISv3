package ru.iris.commons.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.speak.SpeakEvent;

@Component
public class SpeakHelper {

    private final EventBus r;

    @Autowired
    public SpeakHelper(EventBus r) {
        this.r = r;
    }

    public void say(String text) {
        if (text != null && !text.isEmpty()) {
            r.notify("event.speak", Event.wrap(new SpeakEvent(text)));
        }
    }

}
