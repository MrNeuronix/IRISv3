package ru.iris.models.protocol.enums;

import lombok.Getter;

public enum StandartDeviceValueLabel {
    LEVEL("level"),
    LEVEL_ON_SUBCHANNEL_1("level1"),
    LEVEL_ON_SUBCHANNEL_2("level2"),

    BATTERY("battery"),

    STATUS("status"),

    TEMPERATURE("temperature"),
    HUMIDITY("humidity"),
    VOLTAGE("voltage"),

    ILLUMINANCE("illuminance"),

    MOTION("motion"),
    NO_MOTION("nomotion"),

    OPENED("opened"),

    LEAK("leak"),

    BEAMING("beaming"),

    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    SPEED("speed"),

    GPS_DATA("gps")
    ;

    @Getter
    private String name;

    StandartDeviceValueLabel(String name) {
        this.name = name;
    }
}
