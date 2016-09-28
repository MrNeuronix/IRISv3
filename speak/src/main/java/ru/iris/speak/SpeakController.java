package ru.iris.speak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.service.AbstractService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class SpeakController extends AbstractService {

	private final EventBus r;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public SpeakController(EventBus r) {
		this.r = r;
	}

	@Override
	@PostConstruct
	public void onStartup() {
		r.notify("speak.say", Event.wrap("Starting Speak service"));
	}

	@Override
	@PreDestroy
	public void onShutdown()
	{
		r.notify("speak.say", Event.wrap("Shutdown Speak service"));
	}

	@Override
	@PostConstruct
	public void subscribe() {
		addSubscription("speak.say");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return log -> logger.info("Saying: {}", log.getData());
	}
}
