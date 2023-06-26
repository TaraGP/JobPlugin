package org.Ivoyant.service;

import org.Ivoyant.model.Job;
import org.Ivoyant.model.SchedulerStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JobPlugin {
    private String url;
    private String username;
    private String password;

    private static final Logger logger = LoggerFactory.getLogger(JobPlugin.class);
    public  JobPlugin()
    {
        this.url = "jdbc:postgresql://localhost:5432/postgres";
        this.username = "postgres";
        this.password = "password";
    }
    public JobPlugin(Connection connection) {
        this.url = "jdbc:postgresql://localhost:5432/postgres";
        this.username = "postgres";
        this.password = "password";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error in closing database connection: " + e);
            e.printStackTrace();
        }
    }
    public void deleteJob(Long jobId) {
        Connection connection = null;
        try {
            connection = getConnection();
            if (!isJobIdExists(jobId, connection)) {
                System.out.println("Error: Job ID does not exist.");
                return;
            }
            String sql = "DELETE FROM Job WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, jobId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error in deleting job: " + e);
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public List<Job> getAllJobs() {
        Connection connection = null;
        try {
            connection = getConnection();
            String sql = "SELECT * FROM Job";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            List<Job> jobs = new ArrayList<>();
            while (resultSet.next()) {
                Job job = extractJobFromResultSet(resultSet);
                jobs.add(job);
            }
            return jobs;
        } catch (SQLException e) {
            logger.error("Error in getting all jobs: " + e);
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
        return Collections.emptyList();
    }
    public Job getJob(Long jobId) {
        Connection connection = null;

        try {
            connection = getConnection();
            if (!isJobIdExists(jobId, connection)) {
                System.out.println("Error: Job ID does not exist.");
                return null;
            }
            String sql = "SELECT * FROM Job WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, jobId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractJobFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error("Error in getting job: " + e);
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error("Error in getting job: " + e);
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
        return null;
    }

    public void createJob(Job job) {
        Connection connection = null;
        try {
            connection = getConnection();
            if (isJobIdExists(job.getId(), connection)) {
                System.out.println("Error: Job ID already exists.");
                return;
            }
            String sql = "INSERT INTO Job (id, name, description, target, active, targetType, cronExpression,createdTime, lastRun, nextRun,  schedulerState, zoneId) VALUES (?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?::scheduler_state_enum, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setLong(1, job.getId());
            statement.setString(2, job.getName());
            statement.setString(3, job.getDescription());
            statement.setString(4, job.getTarget());
            statement.setBoolean(5, job.isActive());
            statement.setString(6, job.getTargetType());
            statement.setString(7, job.getCronExpression());
            statement.setTimestamp(8,job.getCreatedTime());
            statement.setTimestamp(9, null);
            statement.setTimestamp(10, job.getNextRun());
            statement.setString(11, String.valueOf(SchedulerStateEnum.CREATED));
            statement.setString(12,job.getZoneId());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error("Error in creating job: " + e);
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void updateJob(Job job) {
        Connection connection = null;
        try {
            connection = getConnection();
            if (job.getCronExpression() == null) {
                throw new IllegalArgumentException("cronExpression cannot be null");
            }

            // Check if the job ID is present
            if (isJobIdExists(job.getId(), connection)) {
                String sql = "UPDATE Job SET name = ?, description = ?, target = ?, active = ?, targetType = ?, cronExpression = ?, createdTime = ?, lastRun = ?, nextRun = ?, schedulerState = ?::scheduler_state_enum, zoneId = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setString(1, job.getName());
                statement.setString(2, job.getDescription());
                statement.setString(3, job.getTarget());
                statement.setBoolean(4, job.isActive());
                statement.setString(5, job.getTargetType());
                statement.setString(6, job.getCronExpression());
                statement.setTimestamp(7, job.getCreatedTime());
                statement.setTimestamp(8, job.getLastRun());
                statement.setTimestamp(9, job.getNextRun());
                statement.setString(10, String.valueOf(SchedulerStateEnum.BUFFERED));
                statement.setString(11, job.getZoneId());
                statement.setLong(12, job.getId());

                statement.executeUpdate();
            } else {
                throw new IllegalArgumentException("Job with ID " + job.getId() + " does not exist");
            }
        } catch (SQLException e) {
            logger.error("Error in updating job: " + e);
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    private boolean isJobIdExists(Long jobId, Connection connection) throws SQLException {
        String sql = "SELECT id FROM Job WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, jobId);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }


    private void moveJobToBufferedDb(Job job, Connection connection) {
        try {
            String sql = "INSERT INTO BufferedJobs (id, name, description, target, active, targetType, cronExpression, createdTime, lastRun, nextRun, schedulerState,zoneId) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::scheduler_state_enum,?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setLong(1, job.getId());
            statement.setString(2, job.getName());
            statement.setString(3, job.getDescription());
            statement.setString(4, job.getTarget());
            statement.setBoolean(5, job.isActive());
            statement.setString(6, job.getTargetType());
            statement.setString(7, job.getCronExpression());
            statement.setTimestamp(8, job.getCreatedTime());
            statement.setTimestamp(9, job.getLastRun());
            statement.setTimestamp(10, job.getNextRun());
            statement.setString(11, job.getSchedulerState().toString());
            statement.setString(12,job.getZoneId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error in moving job to BufferedDb: " + e);
            e.printStackTrace();
        }
    }
    // Helper method to extract a Job object from a ResultSet
    private Job extractJobFromResultSet(ResultSet resultSet) throws SQLException, ParseException {
        Job job = new Job();
        job.setId(resultSet.getLong("id"));
        job.setName(resultSet.getString("name"));
        job.setDescription(resultSet.getString("description"));
        job.setTarget(resultSet.getString("target"));
        job.setActive(resultSet.getBoolean("active"));
        job.setTargetType(resultSet.getString("targetType"));
        job.setCronExpression(resultSet.getString("cronExpression"));

        Timestamp createdTimeTimestamp = resultSet.getTimestamp("createdTime");
        if (createdTimeTimestamp != null) {
            job.setCreatedTime(new Timestamp(createdTimeTimestamp.getTime()));
        }

        Timestamp lastRunTimestamp = resultSet.getTimestamp("lastRun");
        if (lastRunTimestamp != null) {
            job.setLastRun(new Timestamp(lastRunTimestamp.getTime()));
        }

        Timestamp nextRunTimestamp = resultSet.getTimestamp("nextRun");
        if (nextRunTimestamp != null) {
            job.setNextRun(new Timestamp(nextRunTimestamp.getTime()));
        }


        String schedulerStateString = resultSet.getString("schedulerState");
        if (schedulerStateString != null) {
            job.setSchedulerState(SchedulerStateEnum.valueOf(schedulerStateString));
        }

        String zoneIdString = resultSet.getString("zoneId");
        if (zoneIdString != null) {
            job.setZoneId(String.valueOf(ZoneId.of(zoneIdString)));
        }

        return job;
    }

}

