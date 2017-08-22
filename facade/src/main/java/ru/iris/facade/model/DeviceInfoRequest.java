package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;
import ru.iris.commons.protocol.enums.SourceProtocol;

@Getter
@Setter
public class DeviceInfoRequest {
    private SourceProtocol source;
    private String channel;
}
