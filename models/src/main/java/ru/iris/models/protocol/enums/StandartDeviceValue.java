package ru.iris.models.protocol.enums;

import lombok.Getter;

public enum StandartDeviceValue {
    FULL_ON("255"),
    FULL_OFF("0"),;

    @Getter
    private String value;

    StandartDeviceValue(String value) {
        this.value = value;
    }

    public static StandartDeviceValue parse(String name) {
        for (StandartDeviceValue label : StandartDeviceValue.values()) {
            if (label.value.equals(name))
                return label;
        }

        return null;
    }
}
