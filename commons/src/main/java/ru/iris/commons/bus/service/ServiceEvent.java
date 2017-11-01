package ru.iris.commons.bus.service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.iris.commons.bus.Event;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ServiceEvent implements Event {
    private String label;
}
