package ru.iris.facade.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;
import reactor.bus.EventBus;
import ru.iris.models.bus.terminal.TerminalEvent;

@Slf4j
@Controller
public class WSFacade {

	@Autowired
	private EventBus r;

	@MessageMapping("/stomp")
	public void incomingMessager(TerminalEvent message) throws Exception {
		logger.info("Message from terminal has arrive: " + message);
		r.notify(message.getQueue(), reactor.bus.Event.wrap(message));
	}
}
