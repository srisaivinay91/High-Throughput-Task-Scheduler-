package com.taskscheduler.model;

/**
 * TaskStatus enum representing the lifecycle states of a task
 * 
 * Provides comprehensive status tracking for tasks in the distributed
 * scheduling system, enabling proper workflow management and monitoring.
 * 
 * Status Flow:
 * PENDING -> QUEUED -> RUNNING -> [COMPLETED | FAILED] 
 *    |         |         |
 *    v         v         v
 * CANCELLED CANCELLED  CANCELLED
 *                        |
 *                        v
 *                    RETRYING -> RUNNING
 */
public enum TaskStatus {
    PENDING("Task created and waiting to be queued"),
    QUEUED("Task added to priority queue, waiting for worker"),
    RUNNING("Task currently being executed by a worker"),
    COMPLETED("Task executed successfully"),
    FAILED("Task execution failed permanently"),
    CANCELLED("Task was cancelled before or during execution"),
    RETRYING("Task failed but will be retried"),
    PAUSED("Task execution temporarily paused"),
    SCHEDULED("Task scheduled for future execution");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the task is in a terminal state
     * Terminal states indicate the task has finished processing
     * 
     * @return true if the task is in a final state
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * Check if the task is in an active state
     * Active states indicate the task is currently being processed
     * 
     * @return true if the task is actively being processed
     */
    public boolean isActive() {
        return this == RUNNING || this == RETRYING;
    }

    /**
     * Check if the task is waiting for execution
     * Waiting states indicate the task is ready but not yet running
     * 
     * @return true if the task is waiting to be executed
     */
    public boolean isWaiting() {
        return this == PENDING || this == QUEUED || this == SCHEDULED;
    }

    /**
     * Check if the task can be cancelled
     * 
     * @return true if the task can be cancelled
     */
    public boolean isCancellable() {
        return !isTerminal() && this != CANCELLED;
    }

    /**
     * Check if the task can be retried
     * 
     * @return true if the task can be retried
     */
    public boolean isRetriable() {
        return this == FAILED || this == RETRYING;
    }

    /**
     * Get the next status for retry scenarios
     * 
     * @return the appropriate status for retry
     */
    public TaskStatus getRetryStatus() {
        if (this == FAILED) {
            return RETRYING;
        }
        if (this == RETRYING) {
            return QUEUED;
        }
        throw new IllegalStateException("Cannot get retry status for: " + this);
    }

    /**
     * Check if status transition is valid
     * 
     * @param from source status
     * @param to target status
     * @return true if transition is valid
     */
    public static boolean isValidTransition(TaskStatus from, TaskStatus to) {
        if (from == to) {
            return true;
        }

        return switch (from) {
            case PENDING -> to == QUEUED || to == CANCELLED || to == SCHEDULED;
            case QUEUED -> to == RUNNING || to == CANCELLED;
            case RUNNING -> to == COMPLETED || to == FAILED || to == CANCELLED || to == PAUSED;
            case FAILED -> to == RETRYING || to == CANCELLED;
            case RETRYING -> to == QUEUED || to == CANCELLED;
            case PAUSED -> to == QUEUED || to == CANCELLED;
            case SCHEDULED -> to == QUEUED || to == CANCELLED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };
    }

    /**
     * Get status from string with case-insensitive matching
     * 
     * @param value String representation of status
     * @return TaskStatus enum value
     * @throws IllegalArgumentException if value doesn't match any status
     */
    public static TaskStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status value cannot be null");
        }
        
        try {
            return TaskStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid task status: " + value + 
                ". Valid values are: " + java.util.Arrays.toString(values()));
        }
    }

    @Override
    public String toString() {
        return name() + " - " + description;
    }
}