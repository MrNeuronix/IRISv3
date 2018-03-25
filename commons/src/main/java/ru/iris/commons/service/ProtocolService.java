package ru.iris.commons.service;

import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.models.bus.Queue;

public interface ProtocolService {
    void onStartup() throws InterruptedException;
    void onShutdown();
    Consumer<Event<?>> handleMessage() throws Exception;
    void subscribe() throws Exception;
    void broadcast(String queue, Object object);
		void broadcast(Queue queue, Object object);
    void run() throws Exception;
}
