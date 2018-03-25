package ru.iris.models.web;

import lombok.Getter;
import lombok.Setter;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
public class DeviceNamingRequest {
    private SourceProtocol source;
    private String channel;
    private String name;
}
