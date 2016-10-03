package ru.iris.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static reactor.bus.selector.Selectors.R;

public abstract class AbstractService implements Service {

	@Autowired
	private EventBus r;

	@Override
	@PostConstruct
	public abstract void onStartup() throws InterruptedException;

	@Override
	@PreDestroy
	public abstract void onShutdown();

	@Override
	public abstract Consumer<Event<?>> handleMessage();

	@Override
	public abstract void subscribe();

	@Override
	public void broadcast(String queue, Object object) {
		r.notify(queue, Event.wrap(object));
	}

	protected void addSubscription(String regex) {
		r.on(R(regex), handleMessage());
	}
}
