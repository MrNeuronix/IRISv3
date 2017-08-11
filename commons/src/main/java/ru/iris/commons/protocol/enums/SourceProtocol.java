package ru.iris.commons.protocol.enums;

public enum SourceProtocol {

    ZWAVE("zwave"),
    NOOLITE("noolite"),
    XIAOMI("xiaomi"),
    HTTP("http"),
    UNKNOWN("unknown");

    private final String name;

    SourceProtocol(String name) {
        this.name = name;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }

}
