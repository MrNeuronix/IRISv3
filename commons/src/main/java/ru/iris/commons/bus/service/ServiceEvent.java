package ru.iris.commons.bus.service;

import lombok.*;
import ru.iris.commons.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@AllArgsConstructor
public class ServiceEvent implements Event {
    private String label;
}
