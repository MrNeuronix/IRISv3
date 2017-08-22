package ru.iris.commons.bus.devices;

import lombok.*;
import ru.iris.commons.bus.Event;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractDeviceEvent implements Event {
    protected String channel;
    protected SourceProtocol protocol;
    protected String label;
    protected Object from;
    protected Object to;
    protected ValueType valueType = ValueType.UNKNOWN;
}
