package ru.iris.models.bus.transport;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class TransportPingEvent extends AbstractTransportEvent {
    @Builder
    public TransportPingEvent(int id) {
        super(id);
        this.transportId = id;
    }
}
