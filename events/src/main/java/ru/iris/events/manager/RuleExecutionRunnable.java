package ru.iris.events.manager;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.iris.events.types.Event;
import ru.iris.events.types.Rule;

@Slf4j
public class RuleExecutionRunnable implements Runnable {

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
            logger.error("Error while executing rule: ", e);
        }
    }

}
