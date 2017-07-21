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

public abstract class AbstractService implements Service {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private EventBus r;

    @Override
    @PostConstruct
    public abstract void onStartup() throws InterruptedException;

    @Override
    @PreDestroy
    public abstract void onShutdown();

    @Override
    public abstract Consumer<Event<?>> handleMessage() throws Exception;

    @Override
    @PostConstruct
    public abstract void subscribe() throws Exception;

    @Override
    @Async
    public abstract void run();

    @Override
    public void broadcast(String queue, Object object) {
        r.notify(queue, Event.wrap(object));
    }

    protected void addSubscription(String regex) throws Exception {
        logger.info("Binding on: {}", regex);
        r.on(R(regex), handleMessage());
    }
}
