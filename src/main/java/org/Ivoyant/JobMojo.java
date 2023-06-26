package org.Ivoyant;

import org.Ivoyant.service.JobPlugin;
import org.Ivoyant.model.Job;
import org.Ivoyant.model.SchedulerStateEnum;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Mojo(name = "job-plugin", requiresProject = false, requiresDependencyResolution = ResolutionScope.NONE)
public class JobMojo extends AbstractMojo {

    @Parameter(property = "jobId")
    private Long jobId;

    @Parameter(property = "jobName")
    private String jobName;

    @Parameter(property = "jobDescription")
    private String jobDescription;

    @Parameter(property = "jobTarget")
    private String jobTarget;

    @Parameter(property = "jobActive")
    private boolean jobActive;

    @Parameter(property = "jobTargetType")
    private String jobTargetType;

    @Parameter(property = "jobCronExpression")
    private String jobCronExpression;

    @Parameter(property = "jobCreatedTime")
    private String jobCreatedTime;

    @Parameter(property = "jobLastRun")
    private String jobLastRun;

    @Parameter(property = "jobNextRun")
    private String jobNextRun;
    @Parameter(property="jobZoneId")
    private ZoneId jobZoneId;

    @Parameter(property = "databaseUrl")
    private String databaseUrl = "jdbc:postgresql://localhost:5432/postgres";

    @Parameter(property = "databaseUsername")
    private String databaseUsername = "postgres";

    @Parameter(property = "databasePassword")
    private String databasePassword = "password";

    static final Logger logger = LoggerFactory.getLogger(JobMojo.class);

    public void execute() throws MojoExecutionException
    {
        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword))
        {
        JobPlugin jobPlugin = new JobPlugin(connection);

        if (jobId != null) {
            // Retrieve a job by ID
            Job job = jobPlugin.getJob(jobId);
            if (job != null) {
                getLog().info("Retrieved Job: " + job.toString());
            } else {
                getLog().info("Job with ID " + jobId + " not found.");
            }
        }
        else
        {
            // Create a new job
            Job newJob = new Job();
            newJob.setName(jobName);
            newJob.setDescription(jobDescription);
            newJob.setTarget(jobTarget);
            newJob.setActive(jobActive);
            newJob.setTargetType(jobTargetType);
            newJob.setCronExpression(jobCronExpression);

            // Set the createdTime, lastRun, and nextRun using GMT time zone
            LocalDateTime createdTime = LocalDateTime.parse(jobCreatedTime);
            newJob.setCreatedTime(Timestamp.valueOf(ZonedDateTime.of(createdTime, ZoneOffset.UTC).toLocalDateTime()));

            LocalDateTime lastRun = LocalDateTime.parse(jobLastRun);
            newJob.setLastRun(Timestamp.valueOf(ZonedDateTime.of(lastRun, ZoneOffset.UTC).toLocalDateTime()));

            LocalDateTime nextRun = LocalDateTime.parse(jobNextRun);
            newJob.setNextRun(Timestamp.valueOf(ZonedDateTime.of(nextRun, ZoneOffset.UTC).toLocalDateTime()));

            newJob.setSchedulerState(SchedulerStateEnum.CREATED);
            newJob.setZoneId(String.valueOf(jobZoneId));
            jobPlugin.createJob(newJob);
            getLog().info("New Job created: " + newJob.toString());
        }
    } catch (SQLException e) {
        logger.error("Error executing job-plugin: " + e);
        throw new MojoExecutionException("Error executing job-plugin", e);
    }
}

}


