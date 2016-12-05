package ru.iris.events.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.iris.events.types.Event;
import ru.iris.events.types.Rule;

public class RuleExecutionRunnable implements Runnable {
	static private final Logger logger = LoggerFactory.getLogger(RuleExecutionRunnable.class);

	private Rule rule;
	private Event event;

	public RuleExecutionRunnable(Rule rule, Event event) {
		this.rule = rule;
		this.event = event;
	}

	@Override
	public void run() {
		try {
			this.rule.execute(event);
		} catch (Exception e) {
			logger.error("Error while executing rule: " + rule, e);
		}
	}

}
