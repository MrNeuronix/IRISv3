package ru.iris.models.bus.transport;

import lombok.*;

import java.time.Instant;

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
    private Integer satellites;
    private Instant time = Instant.now();

    @Builder
    public GPSDataEvent(Double latitude, Double longitude, Double speed, Double elevation, Instant time,
                        Integer satellites, int id) {
        super(id);
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time == null ? Instant.now() : time;
        this.satellites = satellites;
        this.transportId = id;
    }
}
