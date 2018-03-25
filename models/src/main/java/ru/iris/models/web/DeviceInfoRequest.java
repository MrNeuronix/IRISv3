package ru.iris.models.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
@AllArgsConstructor
public class DeviceInfoRequest {
    private SourceProtocol source;
    private String channel;
}
