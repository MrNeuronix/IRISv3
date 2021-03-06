package ru.iris.models.bus.service;

import lombok.*;
import ru.iris.models.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEvent extends Event {
    private String label;
    private String identifier;
    private Object data;
}
