package ru.iris.models.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
@NoArgsConstructor
public class DeviceNamingRequest {
    private SourceProtocol source;
    private String channel;
    private String name;
}
