package ru.iris.commons.protocol.enums;

import lombok.Getter;

public enum EventLabel {
    TURN_ON("TurnOn"),
    TURN_OFF("TurnOff"),
    SET_LEVEL("SetLevel"),

    BIND_RX("BindRX"),
    UNBIND_RX("UnbindRX"),
    UNBIND_ALL_RX("UnbindAllRX"),

    BIND("Bind"),
    UNBIND("Unbind"),

    CANCEL("Cancel"),

    BIND_TX("BindTX"),
    UNBIND_TX("UnbindTX"),
    UNBIND_ALL_TX("UnbindAllTX"),

    STOP_DIM_BRIGHT("StopDimBright"),

    BATTERY_LOW("BatteryLow");

    @Getter
    private String name;

    EventLabel(String name) {
        this.name = name;
    }
}
