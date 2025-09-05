package com.taskscheduler.repository;

import com.taskscheduler.model.Priority;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task entity operations
 * 
 * Provides optimized database operations for high-throughput task processing
 * including priority-based retrieval, batch operations, and concurrent access safety.
 * 
 * Features:
 * - Priority-based task retrieval for queue processing
 * - Pessimistic locking for concurrent worker safety
 * - Batch operations for high-throughput scenarios
 * - Comprehensive query methods for monitoring and management
 * - Optimized indexing support for performance
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks ready for execution ordered by priority and creation time
     * Uses pessimistic locking to prevent concurrent execution by multiple workers
     * 
     * @param status Task status to filter by (typically PENDING or QUEUED)
     * @param executionTime Current time for scheduling check
     * @param pageable Pagination parameters
     * @return List of tasks ready for execution
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.status = :status AND " +
           "(t.nextExecutionTime IS NULL OR t.nextExecutionTime <= :executionTime) " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findTasksReadyForExecution(
        @Param("status") TaskStatus status,
        @Param("executionTime") LocalDateTime executionTime,
        Pageable pageable
    );

    /**
     * Find tasks by status and priority with pagination
     * 
     * @param status Task status to filter by
     * @param priority Priority level to filter by
     * @param pageable Pagination parameters
     * @return Page of matching tasks
     */
    Page<Task> findByStatusAndPriorityOrderByCreatedAtDesc(
        TaskStatus status, 
        Priority priority, 
        Pageable pageable
    );

    /**
     * Find tasks by status ordered by priority and creation time
     * 
     * @param status Task status to filter by
     * @param pageable Pagination parameters
     * @return Page of matching tasks
     */
    Page<Task> findByStatusOrderByPriorityDescCreatedAtAsc(
        TaskStatus status, 
        Pageable pageable
    );

    /**
     * Find tasks scheduled for execution before a specific time
     * 
     * @param executionTime Time threshold
     * @param pageable Pagination parameters
     * @return Page of scheduled tasks
     */
    @Query("SELECT t FROM Task t WHERE t.nextExecutionTime <= :executionTime " +
           "AND t.status IN ('SCHEDULED', 'PENDING') " +
           "ORDER BY t.priority DESC, t.nextExecutionTime ASC")
    Page<Task> findTasksScheduledBefore(
        @Param("executionTime") LocalDateTime executionTime,
        Pageable pageable
    );

    /**
     * Find tasks by task type and status
     * 
     * @param taskType Type of task
     * @param status Task status
     * @param pageable Pagination parameters
     * @return Page of matching tasks
     */
    Page<Task> findByTaskTypeAndStatus(
        String taskType, 
        TaskStatus status, 
        Pageable pageable
    );

    /**
     * Count tasks by status
     * 
     * @param status Task status
     * @return Number of tasks with the specified status
     */
    long countByStatus(TaskStatus status);

    /**
     * Count tasks by priority
     * 
     * @param priority Task priority
     * @return Number of tasks with the specified priority
     */
    long countByPriority(Priority priority);

    /**
     * Count tasks created within a time range
     * 
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return Number of tasks created in the time range
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find tasks that are stuck in running status for too long
     * 
     * @param stuckThreshold Time threshold for considering a task stuck
     * @param pageable Pagination parameters
     * @return Page of potentially stuck tasks
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING' AND " +
           "t.lastExecutedAt IS NOT NULL AND " +
           "t.lastExecutedAt < :stuckThreshold")
    Page<Task> findStuckTasks(
        @Param("stuckThreshold") LocalDateTime stuckThreshold,
        Pageable pageable
    );

    /**
     * Find tasks eligible for retry
     * 
     * @param currentTime Current timestamp
     * @param pageable Pagination parameters
     * @return Page of tasks that can be retried
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'FAILED' AND " +
           "t.currentRetryCount < t.maxRetryAttempts AND " +
           "(t.lastExecutedAt IS NULL OR t.lastExecutedAt < :currentTime)")
    Page<Task> findTasksEligibleForRetry(
        @Param("currentTime") LocalDateTime currentTime,
        Pageable pageable
    );

    /**
     * Update task status with optimistic locking
     * 
     * @param taskId Task ID
     * @param newStatus New status to set
     * @param version Current version for optimistic locking
     * @return Number of updated records
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :taskId AND t.version = :version")
    int updateTaskStatus(
        @Param("taskId") Long taskId,
        @Param("newStatus") TaskStatus newStatus,
        @Param("version") Long version
    );

    /**
     * Batch update task statuses
     * 
     * @param taskIds List of task IDs to update
     * @param newStatus New status to set
     * @return Number of updated records
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id IN :taskIds")
    int updateTaskStatusBatch(
        @Param("taskIds") List<Long> taskIds,
        @Param("newStatus") TaskStatus newStatus
    );

    /**
     * Delete completed tasks older than specified date
     * 
     * @param beforeDate Date threshold
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM Task t WHERE t.status = 'COMPLETED' AND t.updatedAt < :beforeDate")
    int deleteCompletedTasksOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Find next task to execute with pessimistic lock
     * Atomic operation for getting the next highest priority task
     * 
     * @param status Task status to filter by
     * @param executionTime Current time for scheduling check
     * @return Optional task ready for execution
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.status = :status AND " +
           "(t.nextExecutionTime IS NULL OR t.nextExecutionTime <= :executionTime) " +
           "ORDER BY t.priority DESC, t.createdAt ASC LIMIT 1")
    Optional<Task> findNextTaskForExecution(
        @Param("status") TaskStatus status,
        @Param("executionTime") LocalDateTime executionTime
    );

    /**
     * Get task statistics for monitoring
     * 
     * @return Task statistics grouped by status
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> getTaskStatsByStatus();

    /**
     * Get task performance metrics
     * 
     * @param fromTime Start time for metrics
     * @return Performance statistics
     */
    @Query("SELECT AVG(t.executionDurationMs), MIN(t.executionDurationMs), MAX(t.executionDurationMs), COUNT(t) " +
           "FROM Task t WHERE t.status = 'COMPLETED' AND t.lastExecutedAt >= :fromTime")
    Object[] getPerformanceMetrics(@Param("fromTime") LocalDateTime fromTime);
}