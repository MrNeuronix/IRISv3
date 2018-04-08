package ru.iris.models.protocol.enums;

public enum SourceProtocol {

    ZWAVE("zwave"),
    NOOLITE("noolite"),
    XIAOMI("xiaomi"),
    HTTP("http"),
    TRANSPORT("transport"),
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
