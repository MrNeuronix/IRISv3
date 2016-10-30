package ru.iris.commons.helpers;

import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.speak.SpeakEvent;

/**
 * Created by nix on 30.10.16.
 */
public class SpeakHelper {

	public static void say(EventBus r, String text) {
		if(text != null && !text.isEmpty()) {
			r.notify("event.speak", Event.wrap(new SpeakEvent(text)));
		}
	}

}
