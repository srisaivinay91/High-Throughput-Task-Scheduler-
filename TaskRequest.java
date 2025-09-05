package com.taskscheduler.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskscheduler.model.Priority;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Task Request DTO for creating new tasks
 * 
 * This DTO provides a clean API interface for task creation requests
 * with comprehensive validation and documentation for clients.
 * 
 * Features:
 * - Input validation for all required fields
 * - Flexible scheduling options
 * - Priority-based execution control
 * - Timeout and retry configuration
 */
public class TaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name must not exceed 255 characters")
    private String taskName;

    @NotBlank(message = "Task type is required")
    @Size(max = 100, message = "Task type must not exceed 100 characters")
    private String taskType;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private String payload;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledTime;

    @Min(value = 1, message = "Execution timeout must be at least 1 second")
    @Max(value = 3600, message = "Execution timeout must not exceed 3600 seconds")
    private Integer executionTimeoutSeconds = 300;

    @Min(value = 0, message = "Max retry attempts cannot be negative")
    @Max(value = 10, message = "Max retry attempts must not exceed 10")
    private Integer maxRetryAttempts = 3;

    // Constructors
    public TaskRequest() {
        this.priority = Priority.MEDIUM;
        this.scheduledTime = LocalDateTime.now();
    }

    public TaskRequest(String taskName, String taskType, Priority priority, String payload) {
        this();
        this.taskName = taskName;
        this.taskType = taskType;
        this.priority = priority;
        this.payload = payload;
    }

    // Getters and Setters
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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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

    @Override
    public String toString() {
        return "TaskRequest{" +
                "taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", priority=" + priority +
                ", scheduledTime=" + scheduledTime +
                ", executionTimeoutSeconds=" + executionTimeoutSeconds +
                ", maxRetryAttempts=" + maxRetryAttempts +
                '}';
    }
}