package ru.iris.commons.service;

public interface RunnableService {
    void onStartup() throws InterruptedException;

    void onShutdown();

    void run() throws Exception;
}
