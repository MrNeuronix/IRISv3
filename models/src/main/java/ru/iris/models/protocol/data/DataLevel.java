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
public class DataLevel extends EventData {
    private String from;
    private String to;
    private ValueType type;

    public DataLevel(String to, ValueType type) {
        this.to = to;
        this.type = type;
    }
}
