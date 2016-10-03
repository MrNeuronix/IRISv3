package ru.iris.speak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.service.AbstractService;
import ru.iris.commons.service.Speak;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Profile("yandex")
public class YandexController extends AbstractService implements Speak {

	@Autowired
	private EventBus r;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	@PostConstruct
	public void onStartup() {
		r.notify("speak.say", Event.wrap("Starting Yandex Speak service"));
	}

	@Override
	@PreDestroy
	public void onShutdown()
	{
		r.notify("speak.say", Event.wrap("Shutdown Yandex Speak service"));
	}

	@Override
	@PostConstruct
	public void subscribe() {
		addSubscription("speak.say");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return log -> logger.info("Saying (Yandex): {}", log.getData());
	}
}
