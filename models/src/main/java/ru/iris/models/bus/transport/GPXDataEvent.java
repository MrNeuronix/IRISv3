package ru.iris.models.bus.transport;

import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GPXDataEvent extends AbstractTransportEvent {
    private String xml;

    @Builder
    public GPXDataEvent(String xml, int id) {
        super(id);
        this.xml = xml;
        this.transportId = id;
    }
}
