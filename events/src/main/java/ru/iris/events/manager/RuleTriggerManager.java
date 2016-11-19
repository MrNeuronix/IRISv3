package ru.iris.events.manager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.iris.commons.protocol.Device;
import ru.iris.events.types.*;

import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Scope("singleton")
public class RuleTriggerManager {

	private static final Logger logger = LoggerFactory.getLogger(RuleTriggerManager.class);

	// lookup maps for different triggering conditions
	private Map<String, Set<Rule>> changedEventTriggeredRules = Maps.newHashMap();
	private Map<String, Set<Rule>> commandEventTriggeredRules = Maps.newHashMap();
	private List<Rule> systemStartupTriggeredRules = Lists.newArrayList();
	private List<Rule> systemShutdownTriggeredRules = Lists.newArrayList();
	private List<Rule> timerEventTriggeredRules = Lists.newArrayList();

	// the scheduler used for timer events
	private Scheduler scheduler;

	public RuleTriggerManager() {

		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("initializing scheduler throws exception", e);
		}
	}

	/**
	 * Returns all rules which have a trigger of a given type
	 * 
	 * @param type
	 *            the trigger type of the rules to return
	 * @return rules with triggers of the given type
	 */
	public Iterable<Rule> getRules(TriggerType type) {
		Iterable<Rule> result;
		switch (type) {
		case STARTUP:
			result = systemStartupTriggeredRules;
			break;
		case SHUTDOWN:
			result = systemShutdownTriggeredRules;
			break;
		case TIMER:
			result = timerEventTriggeredRules;
			break;
		case CHANGE:
			result = Iterables.concat(changedEventTriggeredRules.values());
			break;
		case COMMAND:
			result = Iterables.concat(commandEventTriggeredRules.values());
			break;
		default:
			result = Sets.newHashSet();
		}
		return result;
	}

	public Iterable<Rule> getRules(TriggerType triggerType, Device device) {
		return internalGetRules(triggerType, device);
	}


	private Iterable<Rule> getAllRules(TriggerType type, String itemName) {
		switch (type) {
		case STARTUP:
			return systemStartupTriggeredRules;
		case SHUTDOWN:
			return systemShutdownTriggeredRules;
		case CHANGE:
			return changedEventTriggeredRules.get(itemName);
		case COMMAND:
			return commandEventTriggeredRules.get(itemName);
		default:
			return Sets.newHashSet();
		}
	}

	private Iterable<Rule> internalGetRules(TriggerType triggerType, Device device) {
		List<Rule> result = Lists.newArrayList();
		String ident = device == null ? "undef" : device.getHumanReadableName();

		Iterable<Rule> rules = getAllRules(triggerType, ident);
		if (rules == null) {
			rules = Lists.newArrayList();
		}
		switch (triggerType) {
		case STARTUP:
			return systemStartupTriggeredRules;
		case SHUTDOWN:
			return systemShutdownTriggeredRules;
		case TIMER:
			return timerEventTriggeredRules;
		case CHANGE:
				for (Rule rule : rules) {
					for (EventTrigger t : rule.getEventTrigger()) {
						if (t.evaluate(device, triggerType)) {
							result.add(rule);
							break;
						}
					}
				}
			break;
		case COMMAND:
			for (Rule rule : rules) {
				for (EventTrigger t : rule.getEventTrigger()) {
					if (t.evaluate(device, triggerType)) {
						result.add(rule);
						break;
					}
				}
			}
			break;
		}

		return result;
	}

	/**
	 * Removes all rules with a given trigger type from the mapping tables.
	 * 
	 * @param type
	 *            the trigger type
	 */
	public void clear(TriggerType type) {
		switch (type) {
		case STARTUP:
			systemStartupTriggeredRules.clear();
			break;
		case SHUTDOWN:
			systemShutdownTriggeredRules.clear();
			break;
		case CHANGE:
			changedEventTriggeredRules.clear();
			break;
		case COMMAND:
			commandEventTriggeredRules.clear();
			break;
		case TIMER:
			for (Rule rule : timerEventTriggeredRules) {
				removeTimerRule(rule);
			}
			timerEventTriggeredRules.clear();
			break;
		}
	}

	/**
	 * Removes all rules from all mapping tables.
	 */
	public void clearAll() {
		clear(TriggerType.STARTUP);
		clear(TriggerType.SHUTDOWN);
		clear(TriggerType.CHANGE);
		clear(TriggerType.COMMAND);
		clear(TriggerType.TIMER);
	}

	/**
	 * Adds a given rule to the mapping tables
	 * 
	 * @param rule
	 *            the rule to add
	 */
	public synchronized void addRule(Rule rule) {
		for (EventTrigger t : rule.getEventTrigger()) {
			// add the rule to the lookup map for the trigger kind
			if (t instanceof StartupTrigger) {
				systemStartupTriggeredRules.add(rule);
			} else if (t instanceof ShutdownTrigger) {
				systemShutdownTriggeredRules.add(rule);
			} else if (t instanceof CommandEventTrigger) {
				CommandEventTrigger ceTrigger = (CommandEventTrigger) t;
				Set<Rule> rules = commandEventTriggeredRules.get(ceTrigger.getItem());
				if (rules == null) {
					rules = new HashSet<Rule>();
					commandEventTriggeredRules.put(ceTrigger.getItem(), rules);
				}
				rules.add(rule);
			} else if (t instanceof ChangedEventTrigger) {
				ChangedEventTrigger ceTrigger = (ChangedEventTrigger) t;
				Set<Rule> rules = changedEventTriggeredRules.get(ceTrigger.getItem());
				if (rules == null) {
					rules = new HashSet<Rule>();
					changedEventTriggeredRules.put(ceTrigger.getItem(), rules);
				}
				rules.add(rule);
			} else if (t instanceof TimerTrigger) {
				timerEventTriggeredRules.add(rule);
				try {
					createTimer(rule, (TimerTrigger) t);
				} catch (SchedulerException e) {
					logger.error("Cannot create timer for rule '{}': {}", rule, e.getMessage());
				}
			}
		}
	}

	/**
	 * Removes a given rule from the mapping tables of a certain trigger type
	 * 
	 * @param type
	 *            the trigger type for which the rule should be removed
	 * @param rule
	 *            the rule to add
	 */
	public void removeRule(TriggerType type, Rule rule) {
		switch (type) {
		case STARTUP:
			systemStartupTriggeredRules.remove(rule);
			break;
		case SHUTDOWN:
			systemShutdownTriggeredRules.remove(rule);
			break;
		case CHANGE:
			changedEventTriggeredRules.remove(rule);
			break;
		case COMMAND:
			commandEventTriggeredRules.remove(rule);
			break;
		case TIMER:
			timerEventTriggeredRules.remove(rule);
			removeTimerRule(rule);
			break;
		}
	}

	public void addRuleModel(List<Rule> rules) {
		for (Rule rule : rules) {
			addRule(rule);
		}
	}

	/**
	 * Removes all rules of a given model (file) from the mapping tables.
	 * 
	 * @param rules
	 *            the given rules to remove
	 */
	public void removeRuleModel(List<Rule> rules) {
		removeRules(TriggerType.CHANGE, changedEventTriggeredRules.values(), rules);
		removeRules(TriggerType.COMMAND, commandEventTriggeredRules.values(), rules);
		removeRules(TriggerType.STARTUP, Collections.singletonList(systemStartupTriggeredRules), rules);
		removeRules(TriggerType.SHUTDOWN, Collections.singletonList(systemShutdownTriggeredRules), rules);
		removeRules(TriggerType.TIMER, Collections.singletonList(timerEventTriggeredRules), rules);
	}

	private void removeRules(TriggerType type, Collection<? extends Collection<Rule>> ruleSets, List<Rule> rules) {
		for (Collection<Rule> ruleSet : ruleSets) {
			for (Rule rule : rules) {
				ruleSet.remove(rule);
				if (type == TriggerType.TIMER) {
					removeTimerRule(rule);
				}
			}
		}
	}

	private void removeTimerRule(Rule rule) {
		try {
			removeTimer(rule);
		} catch (SchedulerException e) {
			logger.error("Cannot remove timer for rule '{}'", rule, e);
		}
	}

	/**
	 * Creates and schedules a new quartz-job and trigger with model and rule name as jobData.
	 * 
	 * @param rule
	 *            the rule to schedule
	 * @param trigger
	 *            the defined trigger
	 * 
	 * @throws SchedulerException
	 *             if there is an internal Scheduler error.
	 */
	private void createTimer(Rule rule, TimerTrigger trigger) throws SchedulerException {
		String cronExpression = trigger.getCron();
		if (cronExpression != null) {
			if (cronExpression.equals("noon")) {
				cronExpression = "0 0 12 * * ?";
			} else if (cronExpression.equals("midnight")) {
				cronExpression = "0 0 0 * * ?";
			}
		} else {
			logger.warn("Unrecognized time expression '{}' in rule '{}'", trigger.getCron(), rule);
			return;
		}

		String jobIdentity = getJobIdentityString(rule, trigger);

		try {
			JobDataMap dataMap = new JobDataMap();
			dataMap.put("rule", rule);
			JobDetail job = newJob(TimeTriggerJob.class).usingJobData(dataMap).withIdentity(jobIdentity).build();
			Trigger quartzTrigger = newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

			scheduler.scheduleJob(job, quartzTrigger);

			logger.debug("Scheduled rule {} with cron expression {}", rule, cronExpression);
		} catch (RuntimeException e) {
			throw new SchedulerException(e.getMessage());
		}
	}

	/**
	 * Delete all {@link Job}s of the DEFAULT group whose name starts with <code>rule.getName()</code>.
	 * 
	 * @throws SchedulerException
	 *             if there is an internal Scheduler error.
	 */
	private void removeTimer(Rule rule) throws SchedulerException {
		Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));
		for (JobKey jobKey : jobKeys) {
			String jobIdentityString = getJobIdentityString(rule, null);
			if (jobKey.getName().startsWith(jobIdentityString)) {
				boolean success = scheduler.deleteJob(jobKey);
				if (!success) {
					logger.warn("Failed to delete cron job '{}'", jobKey.getName());
				} else {
					logger.debug("Removed scheduled cron job '{}'", jobKey.getName());
				}
			}
		}
	}

	private String getJobIdentityString(Rule rule, TimerTrigger trigger) {
		Script script = ScriptManager.getInstance().getScript(rule);
		String jobIdentity = script.getFileName() + "#" + rule;
		if (trigger != null) {
			if (trigger.getCron() != null) {
				jobIdentity += "#" + trigger.getCron();
			}
		}
		return jobIdentity;
	}
}
