package com.taskscheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Task Entity representing a schedulable task in the system
 * 
 * This entity stores all task metadata including priority, payload,
 * execution parameters, and tracking information for high-throughput
 * distributed task processing.
 * 
 * Features:
 * - Priority-based ordering for queue processing
 * - Optimistic locking for concurrent access safety
 * - Comprehensive metadata for monitoring and debugging
 * - Efficient indexing for high-performance queries
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status_priority", columnList = "status, priority DESC, created_at ASC"),
    @Index(name = "idx_task_next_execution", columnList = "next_execution_time ASC"),
    @Index(name = "idx_task_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_task_updated_at", columnList = "updated_at DESC")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "task_name", nullable = false)
    private String taskName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "task_type", nullable = false)
    private String taskType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Size(max = 500)
    @Column(name = "description")
    private String description;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "execution_timeout_seconds")
    private Integer executionTimeoutSeconds = 300; // 5 minutes default

    @Column(name = "max_retry_attempts")
    private Integer maxRetryAttempts = 3;

    @Column(name = "current_retry_count")
    private Integer currentRetryCount = 0;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Task() {
        this.status = TaskStatus.PENDING;
        this.priority = Priority.MEDIUM;
        this.scheduledTime = LocalDateTime.now();
        this.nextExecutionTime = LocalDateTime.now();
    }

    public Task(String taskName, String taskType, Priority priority, String payload) {
        this();
        this.taskName = taskName;
        this.taskType = taskType;
        this.priority = priority;
        this.payload = payload;
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

    // Utility methods
    public boolean isReadyForExecution() {
        return status == TaskStatus.PENDING && 
               (nextExecutionTime == null || nextExecutionTime.isBefore(LocalDateTime.now()));
    }

    public boolean canRetry() {
        return currentRetryCount < maxRetryAttempts;
    }

    public void incrementRetryCount() {
        this.currentRetryCount = (this.currentRetryCount == null) ? 1 : this.currentRetryCount + 1;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", scheduledTime=" + scheduledTime +
                ", createdAt=" + createdAt +
                '}';
    }
}