package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceInfoRequest {
    private String source;
    private Short channel;
}
