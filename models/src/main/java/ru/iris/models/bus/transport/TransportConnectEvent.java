package ru.iris.models.bus.transport;

import lombok.*;
import ru.iris.models.bus.Event;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class TransportConnectEvent extends AbstractTransportEvent {
    public TransportConnectEvent(int id) {
        super();
        this.transportId = id;
    }
}
