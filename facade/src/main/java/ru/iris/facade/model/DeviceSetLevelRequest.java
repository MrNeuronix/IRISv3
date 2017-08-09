package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceSetLevelRequest {
    private String source;
    private Short channel;
    private String level;
}

