package ru.iris.models.bus.transport;

import lombok.*;
import ru.iris.models.bus.Event;
import ru.iris.models.protocol.data.EventData;
import ru.iris.models.protocol.enums.SourceProtocol;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbstractTransportEvent extends Event {
    protected int transportId;
}
