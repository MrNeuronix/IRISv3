package ru.iris.models.bus.event;

import lombok.*;
import ru.iris.models.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandEvent extends Event {
    private String filename;
}
