package ru.iris.models.protocol.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.iris.models.protocol.enums.ValueType;

/**
 * @author Nikolay Viguro, 25.08.17
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataId extends EventData {
    private String id;
}
