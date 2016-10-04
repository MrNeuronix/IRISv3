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
import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("google")
public class GoogleController extends AbstractService implements Speak {

	@Autowired
	private EventBus r;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onStartup() {
		r.notify("speak.say", Event.wrap("Starting Google Speak service"));
	}

	@Override
	public void onShutdown()
	{
		r.notify("speak.say", Event.wrap("Shutdown Google Speak service"));
	}

	@Override
	public void subscribe() throws Exception {
		addSubscription("speak.say");
	}

	@Override
	public void run() {

	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return log -> logger.info("Saying (Google): {}", log.getData());
	}

	@Override
	public void setLanguage(String language) {

	}

	@Override
	public InputStream getMP3Data(String text) throws IOException {
		return null;
	}
}
