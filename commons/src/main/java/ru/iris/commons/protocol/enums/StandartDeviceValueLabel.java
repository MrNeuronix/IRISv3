package ru.iris.commons.protocol.enums;

import lombok.Getter;

public enum StandartDeviceValueLabel {
    LEVEL("level"),
    LEVEL_ON_SUBCHANNEL_1("level1"),
    LEVEL_ON_SUBCHANNEL_2("level2"),

    BATTERY("battery"),

    TEMPERATURE("temperature"),
    HUMIDIDY("humidity"),

    OPENED("opened"),

    BEAMING("beaming");

    @Getter
    private String name;

    StandartDeviceValueLabel(String name) {
        this.name = name;
    }
}
