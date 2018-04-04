package ru.iris.models.service;

/**
 * @author Nikolay Viguro, 04.04.18
 */

public enum ServiceState {
    UNKNOWN,

    STARTING,
    STARTED,

    RUNNING,

    STOPPING,
    STOPPED,

    ERRORED
}
