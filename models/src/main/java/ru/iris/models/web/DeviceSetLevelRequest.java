package ru.iris.models.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSetLevelRequest {
    private SourceProtocol source;
    private String channel;
    private Integer subchannel;
    private String level;
}

