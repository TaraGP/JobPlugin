package org.Ivoyant.util;


import org.Ivoyant.service.JobPlugin;
import org.Ivoyant.model.Job;
import org.Ivoyant.model.SchedulerStateEnum;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@EnableScheduling
@Component
public class ScheduleTask {
    static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);

    private final TaskScheduler taskScheduler;
    private static JobPlugin jobPlugin = new JobPlugin();

    @Autowired
    public ScheduleTask(TaskScheduler taskScheduler, JobPlugin jobPlugin) {
        this.taskScheduler = taskScheduler;
        this.jobPlugin = jobPlugin;
    }

    @Scheduled(fixedRate = 10000)
    public static void executeTask() {
        Instant now = Instant.now();
        Timestamp currentTimestamp = Timestamp.from(now);
        List<Job> jobs = jobPlugin.getAllJobs();

        for (Job job : jobs) {
            if (job.getSchedulerState() == SchedulerStateEnum.CREATED) {
                // Set createdTime to current time
                ZoneId zone=null;
                if(job.getZoneId()==null){
                    zone=ZoneId.of("GMT");
                }
                else {
                    zone=ZoneId.of(job.getZoneId());
                }
                job.setCreatedTime(currentTimestamp);
                job.setLastRun(null); // initial value
                // Calculate nextRun based on cronExpression
                try {
                    CronExpression cronExpression = new CronExpression(job.getCronExpression());
                    ZonedDateTime nextRunDateTime = ZonedDateTime.ofInstant(cronExpression.getNextValidTimeAfter(Date.from(now)).toInstant(), zone);
                    Timestamp nextRunTimestamp = Timestamp.from(nextRunDateTime.toInstant());
                    job.setNextRun(nextRunTimestamp);
                } catch (ParseException e) {
                    System.out.println("Invalid Cron expression for job in JobSchedule: " + job.getId());
                }
            } else if (job.getSchedulerState() != SchedulerStateEnum.CREATED) {
                // Update nextRun and lastRun based on cronExpression
                try {
                    Timestamp lastRun = job.getNextRun();
                    job.setCreatedTime(job.getCreatedTime());

                    // Add null check for lastRun
                    if (lastRun != null) {
                        job.setLastRun(lastRun);
                        CronExpression cronExpression = new CronExpression(job.getCronExpression());
                        ZonedDateTime lastRunDateTime = ZonedDateTime.ofInstant(lastRun.toInstant(), ZoneId.of("GMT"));
                        ZonedDateTime nextRunDateTime = ZonedDateTime.ofInstant(cronExpression.getNextValidTimeAfter(Date.from(lastRunDateTime.toInstant())).toInstant(), ZoneId.of("GMT"));

                        Timestamp nextRunTimestamp = new Timestamp(cronExpression.getNextValidTimeAfter(lastRun).getTime());
                        job.setNextRun(nextRunTimestamp);
                    }
                } catch (ParseException e) {
                    System.out.println("Invalid Cron expression for job in JobSchedule in else: " + job.getId());
                }
            }
            // Update the job in the database
            jobPlugin.updateJob(job);
        }
        System.out.println("Executing task...");
    }

}




