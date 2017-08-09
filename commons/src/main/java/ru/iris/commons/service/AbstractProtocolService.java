package ru.iris.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static reactor.bus.selector.Selectors.R;

public abstract class AbstractProtocolService implements ProtocolService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private EventBus r;

    @PostConstruct
    public abstract void onStartup() throws InterruptedException;

    @PreDestroy
    public abstract void onShutdown();

    public abstract Consumer<Event<?>> handleMessage() throws Exception;

    @PostConstruct
    public abstract void subscribe() throws Exception;

    @Async
    public abstract void run();

    public void broadcast(String queue, Object object) {
        r.notify(queue, Event.wrap(object));
    }

    protected void addSubscription(String regex) throws Exception {
        logger.info("Binding on: {}", regex);
        r.on(R(regex), handleMessage());
    }
}
