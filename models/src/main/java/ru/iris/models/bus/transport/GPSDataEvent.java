package ru.iris.models.bus.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.iris.models.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GPSDataEvent extends AbstractTransportEvent {
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private Double speed;

    @Builder
    public GPSDataEvent(Double latitude, Double longitude, Double speed, Double elevation, int id) {
        super(id);
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.elevation = elevation;
        this.transportId = id;
    }
}
