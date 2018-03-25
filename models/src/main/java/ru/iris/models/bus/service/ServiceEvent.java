package ru.iris.models.bus.service;

import lombok.*;
import ru.iris.models.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@AllArgsConstructor
public class ServiceEvent implements Event {
    private String label;
}
