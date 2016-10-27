package ru.iris.commons.service;

import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.Map;

/**
 * Created by nix on 26.09.2016.
 */

public interface ProtocolService<DEVICE> {

	void onStartup() throws InterruptedException;
	void onShutdown();

	Consumer<Event<?>> handleMessage() throws Exception;
	void subscribe() throws Exception;
	void broadcast(String queue, Object object);
	void run() throws Exception;

	Map<Byte, DEVICE> getDevices();

}
