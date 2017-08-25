package ru.iris.commons.protocol.enums;

import lombok.Getter;

public enum StandartDeviceValue {
    FULL_ON("255"),
    FULL_OFF("0"),;

    @Getter
    private String value;

    StandartDeviceValue(String value) {
        this.value = value;
    }
}
