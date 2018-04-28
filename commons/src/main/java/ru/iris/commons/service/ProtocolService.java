package ru.iris.commons.service;

import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.models.bus.Queue;
import ru.iris.models.service.ServiceState;

public interface ProtocolService extends RunnableService {
    Consumer<Event<?>> handleMessage() throws Exception;
    void subscribe() throws Exception;
    void broadcast(String queue, Object object);

    void broadcast(Queue queue, Object object);

    String getServiceIdentifier();
    ServiceState getServiceStatus();
}
