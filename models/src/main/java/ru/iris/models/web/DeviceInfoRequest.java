package ru.iris.models.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfoRequest {
    private SourceProtocol source;
    private String channel;
}
