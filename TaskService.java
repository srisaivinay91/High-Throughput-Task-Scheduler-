package com.taskscheduler.service;

import com.taskscheduler.dto.TaskRequest;
import com.taskscheduler.dto.TaskResponse;
import com.taskscheduler.model.Priority;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Task Service Interface
 * 
 * Defines the contract for high-throughput task management operations
 * including task creation, scheduling, execution, and monitoring.
 * 
 * This interface provides:
 * - Task lifecycle management
 * - Priority-based scheduling
 * - Batch operations for high throughput
 * - Comprehensive monitoring and statistics
 * - Fault-tolerant execution guarantees
 */
public interface TaskService {

    /**
     * Create and schedule a new task
     * 
     * @param taskRequest Task creation request
     * @return Created task response
     */
    TaskResponse createTask(TaskRequest taskRequest);

    /**
     * Create multiple tasks in batch for high throughput
     * 
     * @param taskRequests List of task creation requests
     * @return List of created task responses
     */
    List<TaskResponse> createTasksBatch(List<TaskRequest> taskRequests);

    /**
     * Get task by ID
     * 
     * @param taskId Task identifier
     * @return Task response if found
     */
    Optional<TaskResponse> getTask(Long taskId);

    /**
     * Get tasks with filtering and pagination
     * 
     * @param status Filter by task status (optional)
     * @param priority Filter by priority (optional)
     * @param taskType Filter by task type (optional)
     * @param pageable Pagination parameters
     * @return Page of task responses
     */
    Page<TaskResponse> getTasks(TaskStatus status, Priority priority, String taskType, Pageable pageable);

    /**
     * Update task status
     * 
     * @param taskId Task identifier
     * @param newStatus New status to set
     * @return Updated task response if successful
     */
    Optional<TaskResponse> updateTaskStatus(Long taskId, TaskStatus newStatus);

    /**
     * Cancel a task
     * 
     * @param taskId Task identifier
     * @return true if task was cancelled successfully
     */
    boolean cancelTask(Long taskId);

    /**
     * Retry a failed task
     * 
     * @param taskId Task identifier
     * @return true if task was queued for retry
     */
    boolean retryTask(Long taskId);

    /**
     * Get task execution statistics
     * 
     * @return Statistics map with various metrics
     */
    java.util.Map<String, Object> getTaskStatistics();

    /**
     * Get tasks scheduled for execution
     * 
     * @param beforeTime Execute before this time
     * @param pageable Pagination parameters
     * @return Page of scheduled tasks
     */
    Page<TaskResponse> getScheduledTasks(LocalDateTime beforeTime, Pageable pageable);

    /**
     * Mark task as completed with execution details
     * 
     * @param taskId Task identifier
     * @param executionDurationMs Execution time in milliseconds
     * @return true if marked successfully
     */
    boolean completeTask(Long taskId, Long executionDurationMs);

    /**
     * Mark task as failed with error information
     * 
     * @param taskId Task identifier
     * @param errorMessage Error description
     * @return true if marked successfully
     */
    boolean failTask(Long taskId, String errorMessage);

    /**
     * Get performance metrics
     * 
     * @param fromTime Start time for metrics calculation
     * @return Performance metrics map
     */
    java.util.Map<String, Object> getPerformanceMetrics(LocalDateTime fromTime);

    /**
     * Clean up old completed tasks
     * 
     * @param olderThan Delete tasks completed before this time
     * @return Number of tasks cleaned up
     */
    int cleanupCompletedTasks(LocalDateTime olderThan);

    /**
     * Get tasks that appear to be stuck in running status
     * 
     * @param stuckThreshold Consider tasks stuck if running longer than this
     * @param pageable Pagination parameters
     * @return Page of potentially stuck tasks
     */
    Page<TaskResponse> getStuckTasks(LocalDateTime stuckThreshold, Pageable pageable);

    /**
     * Reset stuck tasks to pending status
     * 
     * @param stuckThreshold Time threshold for stuck detection
     * @return Number of tasks reset
     */
    int resetStuckTasks(LocalDateTime stuckThreshold);
}