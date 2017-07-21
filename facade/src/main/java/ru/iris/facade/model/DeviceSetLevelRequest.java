package ru.iris.facade.model;

/**
 * Created by nix on 15.11.2016.
 */
public class DeviceSetLevelRequest {

    private String source;
    private Short channel;
    private String level;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Short getChannel() {
        return channel;
    }

    public void setChannel(Short channel) {
        this.channel = channel;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}

