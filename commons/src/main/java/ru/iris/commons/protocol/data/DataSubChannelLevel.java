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
public class DataSubChannelLevel {
    private Integer subChannel;
    private String from;
    private String to;
    private ValueType type;

    public DataSubChannelLevel(Integer subChannel, String to, ValueType type) {
        this.subChannel = subChannel;
        this.to = to;
        this.type = type;
    }
}
