package ru.iris.events.actions;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TimerExecutionJob implements Job {

    private Runnable procedure = null;
    private Timer timer;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("Executing timer '{}'", context.getJobDetail().getKey().toString());
        if (procedure != null) {
            procedure.run();
        }
        timer.setTerminated(true);
    }

    public void setProcedure(Runnable procedure) {
        this.procedure = procedure;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
