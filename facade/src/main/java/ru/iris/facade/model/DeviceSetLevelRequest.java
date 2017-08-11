package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;
import ru.iris.commons.protocol.enums.SourceProtocol;

@Getter
@Setter
public class DeviceSetLevelRequest {
    private SourceProtocol source;
    private Short channel;
    private String level;
}

