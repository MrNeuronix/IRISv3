package ru.iris.events.actions;

import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.quartz.TriggerBuilder.newTrigger;

public class Timer {

	private static final Logger logger = LoggerFactory.getLogger(Timer.class);
	private static Scheduler scheduler;

	static {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (SchedulerException e) {
			logger.error("initializing scheduler throws exception", e);
		}
	}

	private JobKey jobKey;
	private TriggerKey triggerKey;
	private AbstractInstant startTime;

	private boolean cancelled = false;
	private boolean terminated = false;

	public Timer(JobKey jobKey, TriggerKey triggerKey, AbstractInstant startTime) {
		this.jobKey = jobKey;
		this.triggerKey = triggerKey;
		this.startTime = startTime;
	}

	public boolean cancel() {
		try {
			boolean result = scheduler.deleteJob(jobKey);
			if (result) {
				cancelled = true;
			}
		} catch (SchedulerException e) {
			logger.warn("An error occured while cancelling the job '{}': {}", jobKey.toString(), e.getMessage());
		}
		return cancelled;
	}

	public boolean reschedule(AbstractInstant newTime) {
		try {
			Trigger trigger = newTrigger().startAt(newTime.toDate()).build();
			Date nextTriggerTime = scheduler.rescheduleJob(triggerKey, trigger);
			if (nextTriggerTime != null) {
				this.triggerKey = trigger.getKey();
				this.cancelled = false;
				this.terminated = false;
				return true;
			}
		} catch (SchedulerException e) {
			logger.warn("An error occured while rescheduling the job '{}': {}", jobKey.toString(), e.getMessage());
		}
		return false;
	}

	public boolean isRunning() {
		try {
			for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
				if (context.getJobDetail().getKey().equals(jobKey)) {
					return true;
				}
			}
			return false;
		} catch (SchedulerException e) {
			// fallback implementation
			logger.debug("An error occured getting currently running jobs: {}", e.getMessage());
			return DateTime.now().isAfter(startTime) && !terminated;
		}
	}

	public boolean hasTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
}
