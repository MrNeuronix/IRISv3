package ru.iris.models.protocol.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Nikolay Viguro, 25.08.17
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataGPS extends EventData {
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double elevation;
}
