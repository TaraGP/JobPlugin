package org.Ivoyant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.sql.Timestamp;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class Job {
    @Id
    @Column(name ="id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("target")
    private String target;
    @JsonProperty("active")
    private boolean active;
    @JsonProperty("targetType")

    private String targetType;
    @JsonProperty("cronExpression")
    private String cronExpression;
    @JsonProperty("createdTime")
    private Timestamp createdTime;
    @JsonProperty("lastRun")
    private Timestamp lastRun;
    @JsonProperty("nextRun")
    private Timestamp nextRun;
    @JsonProperty("schedulerState")
    @Enumerated(EnumType.STRING)
    private SchedulerStateEnum schedulerState;

    @JsonProperty("zoneId")
    String zoneId ;

    // Getters and Setters
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
    public String getZoneId(){
        return zoneId;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getLastRun() {
        return lastRun;
    }

    public void setLastRun(Timestamp lastRun) {
        this.lastRun = lastRun;
    }

    public Timestamp getNextRun() {
        return nextRun;
    }

    public void setNextRun(Timestamp nextRun) {
        this.nextRun = nextRun;
    }

    public SchedulerStateEnum getSchedulerState() {
        return schedulerState;
    }

    public void setSchedulerState(SchedulerStateEnum schedulerState) {
        this.schedulerState = schedulerState;
    }

    //constructors
    public Job(){}
    public Job(Long id, String name, String description, String target, boolean active, String targetType, String cronExpression, Timestamp createdTime, Timestamp lastRun, Timestamp nextRun, SchedulerStateEnum schedulerState,String zoneId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.target = target;
        this.active = active;
        this.targetType = targetType;
        this.cronExpression = cronExpression;
        this.createdTime = createdTime;
        this.lastRun = lastRun;
        this.nextRun = nextRun;
        this.schedulerState = schedulerState;
        this.zoneId=zoneId;
    }
    // toString() method
    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", target='" + target + '\'' +
                ", active=" + active +
                ", targetType='" + targetType + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", createdTime=" + createdTime +
                ", lastRun=" + lastRun +
                ", nextRun=" + nextRun +
                ", schedulerState=" + schedulerState +
                ",zoneId=" +zoneId+
                '}';
    }
}

