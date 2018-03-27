package ru.iris.models.bus.terminal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.iris.models.bus.Event;
import ru.iris.models.database.Zone;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminalEvent extends Event {
	private String queue;
	private Zone zone;
}
