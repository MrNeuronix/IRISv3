package ru.iris.commons.service;

import reactor.bus.Event;
import reactor.fn.Consumer;

/**
 * Created by nix on 26.09.2016.
 */

public interface Service {

	void onStartup() throws InterruptedException;
	void onShutdown();

	Consumer<Event<?>> handleMessage() throws Exception;
	void subscribe() throws Exception;
	void broadcast(String queue, Object object);
	void run() throws Exception;

}
