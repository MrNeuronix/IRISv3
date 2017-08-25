package ru.iris.commons.protocol.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.iris.commons.protocol.enums.ValueType;

/**
 * @author Nikolay Viguro, 25.08.17
 */

@Getter
@Setter
@AllArgsConstructor
public class DataLevel {
    private String from;
    private String to;
    private ValueType type;

    public DataLevel(String to, ValueType type) {
        this.to = to;
        this.type = type;
    }
}
