package ru.iris.models.bus.devices;

import lombok.*;
import ru.iris.models.bus.Event;
import ru.iris.models.protocol.enums.SourceProtocol;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractDeviceEvent extends Event {
    protected String channel;
    protected SourceProtocol protocol;
    protected String eventLabel;
    protected Object data;
    protected Class clazz;
}
