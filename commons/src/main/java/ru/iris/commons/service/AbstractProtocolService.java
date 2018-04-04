package ru.iris.commons.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.models.bus.Queue;
import ru.iris.models.bus.service.ServiceEvent;
import ru.iris.models.service.ServiceState;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static reactor.bus.selector.Selectors.R;

public abstract class AbstractProtocolService implements ProtocolService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Getter
	private ServiceState serviceState = ServiceState.UNKNOWN;

	@Autowired
	private EventBus r;

	@PostConstruct
	public abstract void onStartup() throws InterruptedException;

	@PreDestroy
	public abstract void onShutdown();

	@Override
	public ServiceState getServiceStatus() {
		return serviceState;
	}

	public abstract Consumer<Event<?>> handleMessage() throws Exception;

	@PostConstruct
	public abstract void subscribe() throws Exception;

	@Async
	public abstract void run() throws InterruptedException;

	public void broadcast(Queue queue, Object object) {
		r.notify(queue.getString(), Event.wrap(object));
	}

	public void broadcast(String queue, Object object) {
		r.notify(queue, Event.wrap(object));
	}

	protected void addSubscription(String queue) throws Exception {
		logger.info("Binding on: {}", queue);
		r.on(R(queue), handleMessage());
	}

	protected void addSubscription(Queue queue) throws Exception {
		logger.info("Binding on: {}", queue.getString());
		r.on(R(queue.getString()), handleMessage());
	}

	protected void setServiceState(ServiceState state) {
		r.notify("event.service.state", Event.wrap(
				ServiceEvent.builder()
						.label("ChangeState")
						.identifier(getServiceIdentifier())
						.data(state)
						.build()
		));
	}
}
