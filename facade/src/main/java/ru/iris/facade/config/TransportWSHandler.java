package ru.iris.facade.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import reactor.bus.EventBus;
import ru.iris.models.bus.Event;

/**
 * @author nix (08.04.2018)
 */
@Component
@Slf4j
public class TransportWSHandler extends TextWebSocketHandler {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EventBus r;

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			Event event = objectMapper.readValue(message.getPayload(), Event.class);
			r.notify("event.transport", reactor.bus.Event.wrap(event));
		} catch (IOException e) {
			logger.error("Error des:", e);
		}

	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		logger.info("WS socket connected...");
	}
}