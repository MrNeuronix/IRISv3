package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.EventBus;
import ru.iris.commons.helpers.SpeakHelper;
import ru.iris.commons.service.Speak;
import ru.iris.facade.model.SpeakRequest;
import ru.iris.facade.model.status.ErrorStatus;
import ru.iris.facade.model.status.OkStatus;

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
	 * @param request request
	 * @return ok or error status
	 */
	@RequestMapping(value = "/api/speak", method = RequestMethod.POST)
	public Object sayAtZone(@RequestBody SpeakRequest request) {

		if(request.getText() != null && !request.getText().isEmpty()) {
			SpeakHelper.say(r, request.getText());
		}
		else {
			new ErrorStatus("empty text passed");
		}

		return new OkStatus("Saying: " + request.getText());
	}
}
