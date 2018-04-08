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
@Builder
public class GPSDataEvent extends Event {
    private Double latitude;
    private Double longitude;
    private Double speed;
    private int transportId;
}
