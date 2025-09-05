package com.taskscheduler.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskscheduler.model.Priority;
import com.taskscheduler.model.TaskStatus;

import java.time.LocalDateTime;

/**
 * Task Response DTO for API responses
 * 
 * Provides a comprehensive view of task information for API clients
 * including execution metadata, timing information, and current status.
 * 
 * Features:
 * - Complete task metadata
 * - Execution tracking information
 * - Formatted timestamps for API consumption
 * - Performance metrics
 */
public class TaskResponse {

    private Long id;
    private String taskName;
    private String taskType;
    private Priority priority;
    private TaskStatus status;
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextExecutionTime;
    
    private Integer executionTimeoutSeconds;
    private Integer maxRetryAttempts;
    private Integer currentRetryCount;
    private String lastErrorMessage;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastExecutedAt;
    
    private Long executionDurationMs;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private Long version;

    // Constructors
    public TaskResponse() {}

    public TaskResponse(Long id, String taskName, String taskType, Priority priority, TaskStatus status) {
        this.id = id;
        this.taskName = taskName;
        this.taskType = taskType;
        this.priority = priority;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public Integer getExecutionTimeoutSeconds() {
        return executionTimeoutSeconds;
    }

    public void setExecutionTimeoutSeconds(Integer executionTimeoutSeconds) {
        this.executionTimeoutSeconds = executionTimeoutSeconds;
    }

    public Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public Integer getCurrentRetryCount() {
        return currentRetryCount;
    }

    public void setCurrentRetryCount(Integer currentRetryCount) {
        this.currentRetryCount = currentRetryCount;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Computed properties
    public boolean isCompleted() {
        return status != null && status.isTerminal();
    }

    public boolean isRunning() {
        return status != null && status.isActive();
    }

    public double getCompletionPercentage() {
        if (status == TaskStatus.COMPLETED) {
            return 100.0;
        }
        if (status == TaskStatus.RUNNING) {
            return 50.0; // Assume 50% when running
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}