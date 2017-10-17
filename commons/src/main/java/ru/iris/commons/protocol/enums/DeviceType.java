package ru.iris.commons.protocol.enums;

public enum DeviceType {

    CONTROLLER("controller"),
    BINARY_SWITCH("switch"),
    MULTILEVEL_SWITCH("dimmer"),
    ALARM_SENSOR("alarmsensor"),
    BINARY_SENSOR("binarysensor"),
    MULTILEVEL_SENSOR("multilevelsensor"),
    SIMPLE_METER("simplemeter"),
    TEMP_HUMI_SENSOR("temphumisensor"),
    TEMP_SENSOR("tempsensor"),
    DOOR_SENSOR("doorsensor"),
    DRAPES("drapes"),
    THERMOSTAT("thermostat"),
    FLOOD_SENSOR("floodsensor"),
    MOTION_SENSOR("motionsensor"),
    UNKNOWN_SENSOR("unknownsensor"),
    BUTTON("button"),
    UNKNOWN("unknown");

    private final String name;

    DeviceType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
