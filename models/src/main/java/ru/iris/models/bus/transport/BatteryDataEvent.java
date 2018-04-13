package ru.iris.models.bus.transport;

import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class BatteryDataEvent extends AbstractTransportEvent {
    private Double voltage;

    @Builder
    public BatteryDataEvent(Double voltage, int id) {
        super(id);
        this.voltage = voltage;
        this.transportId = id;
    }
}
