package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.speak.SpeakEvent;
import ru.iris.commons.service.Speak;
import ru.iris.facade.status.ErrorStatus;
import ru.iris.facade.status.OkStatus;

@RestController
@Profile("facade")
public class SpeakFacade {

	private final Speak speak;
	private EventBus r;

	private static final Logger logger = LoggerFactory.getLogger(SpeakFacade.class);

	@Autowired(required = false)
	public SpeakFacade(
			Speak speak
	)
	{
		this.speak = speak;
	}

	@Autowired
	public void setR(EventBus r) {
		this.r = r;
	}

	/**
	 * Say something on specified zone (or at all zones)
	 *
	 * @param zone zone, where need to speak
	 * @param text text to speak
	 * @return ok or error status
	 */
	@RequestMapping("/api/speak/{zone}")
	public Object sayAtZone(@PathVariable(value = "zone") String zone, @RequestParam(value = "text") String text) {

		boolean all = false;
		if(zone.equals("all"))
			all = true;

		if(text != null && !text.isEmpty()) {
			r.notify("event.speak", Event.wrap(new SpeakEvent(text)));
		}
		else {
			new ErrorStatus("empty text passed");
		}

		return new OkStatus();
	}
}
