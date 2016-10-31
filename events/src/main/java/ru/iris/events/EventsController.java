package ru.iris.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.service.AbstractService;

@Component
@Qualifier("events")
@Profile("events")
@Scope("singleton")
public class EventsController extends AbstractService {

	private final EventBus r;
	private final ConfigLoader config;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public EventsController(EventBus r, ConfigLoader config) {
		this.r = r;
		this.config = config;
	}

	@Override
	public void onStartup() throws InterruptedException {
		logger.info("EventsController started");
	}

	@Override
	public void onShutdown() {
		logger.info("EventsController stopping");
	}

	@Override
	public Consumer<Event<?>> handleMessage() throws Exception {
		return null;
	}

	@Override
	public void subscribe() throws Exception {
		addSubscription("*");
	}

	@Override
	public void run() {

	}
}
