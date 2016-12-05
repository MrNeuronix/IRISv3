package ru.iris.events.manager;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.iris.events.types.Event;
import ru.iris.events.types.Rule;
import ru.iris.events.types.TriggerType;

public class TimeTriggerJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(TimeTriggerJob.class);
	private Rule rule;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String scriptName = ScriptManager.getInstance().getScript(rule).getFileName();
		logger.info("TimeTrigger for rule: " + rule + ", scriptName: " + scriptName);

		ScriptManager manager = ScriptManager.getInstance();

		manager.executeRules(new Rule[] { rule }, new Event(TriggerType.TIMER, null));
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

}
