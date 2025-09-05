package com.taskscheduler.controller;

import com.taskscheduler.dto.TaskRequest;
import com.taskscheduler.dto.TaskResponse;
import com.taskscheduler.model.Priority;
import com.taskscheduler.model.TaskStatus;
import com.taskscheduler.service.TaskService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Task Controller - RESTful API for High-Throughput Task Management
 * 
 * This controller provides comprehensive REST endpoints for:
 * - Task submission and batch operations
 * - Task status tracking and monitoring
 * - Performance metrics and statistics
 * - Administrative operations
 * 
 * API Features:
 * - High-throughput task submission (10,000+ tasks/minute)
 * - Priority-based task scheduling
 * - Comprehensive task lifecycle management
 * - Real-time status tracking
 * - Batch operations for optimal performance
 * - Detailed monitoring and metrics
 * 
 * Performance Optimizations:
 * - Async processing for non-blocking operations
 * - Efficient pagination for large datasets
 * - Metrics collection with Micrometer
 * - Input validation for data integrity
 * 
 * @author Task Scheduler Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    /**
     * Submit a single task for execution
     * 
     * POST /api/v1/tasks
     * 
     * @param taskRequest Task creation request
     * @return Created task response with 201 status
     */
    @PostMapping
    @Timed(value = "task.creation", description = "Time taken to create a task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        logger.info("Creating task: {} with priority: {}", taskRequest.getTaskName(), taskRequest.getPriority());
        
        try {
            TaskResponse response = taskService.createTask(taskRequest);
            logger.info("Task created successfully with ID: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Submit multiple tasks in batch for high-throughput processing
     * 
     * POST /api/v1/tasks/batch
     * 
     * @param taskRequests List of task creation requests
     * @return List of created task responses with 201 status
     */
    @PostMapping("/batch")
    @Timed(value = "task.batch.creation", description = "Time taken to create tasks in batch")
    public ResponseEntity<List<TaskResponse>> createTasksBatch(@Valid @RequestBody List<TaskRequest> taskRequests) {
        logger.info("Creating {} tasks in batch", taskRequests.size());
        
        try {
            List<TaskResponse> responses = taskService.createTasksBatch(taskRequests);
            logger.info("Batch task creation completed: {} tasks created", responses.size());
            return new ResponseEntity<>(responses, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating tasks in batch: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get task by ID with current status and metadata
     * 
     * GET /api/v1/tasks/{taskId}
     * 
     * @param taskId Task identifier
     * @return Task response with current status
     */
    @GetMapping("/{taskId}")
    @Timed(value = "task.retrieval", description = "Time taken to retrieve a task")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId) {
        logger.debug("Retrieving task: {}", taskId);
        
        Optional<TaskResponse> taskResponse = taskService.getTask(taskId);
        
        if (taskResponse.isPresent()) {
            return ResponseEntity.ok(taskResponse.get());
        } else {
            logger.warn("Task not found: {}", taskId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tasks with filtering, sorting, and pagination
     * 
     * GET /api/v1/tasks
     * 
     * @param status Filter by task status (optional)
     * @param priority Filter by priority level (optional)
     * @param taskType Filter by task type (optional)
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param sort Sort criteria (default: createdAt,desc)
     * @return Paginated list of task responses
     */
    @GetMapping
    @Timed(value = "task.list", description = "Time taken to retrieve task list")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String taskType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        logger.debug("Retrieving tasks - status: {}, priority: {}, taskType: {}, page: {}, size: {}", 
                    status, priority, taskType, page, size);
        
        try {
            // Validate and limit page size for performance
            size = Math.min(size, 100);
            
            // Parse sort parameters
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sortObj = Sort.by(direction, sortParams[0]);
            
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<TaskResponse> tasks = taskService.getTasks(status, priority, taskType, pageable);
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error retrieving tasks: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update task status
     * 
     * PUT /api/v1/tasks/{taskId}/status
     * 
     * @param taskId Task identifier
     * @param status New task status
     * @return Updated task response
     */
    @PutMapping("/{taskId}/status")
    @Timed(value = "task.status.update", description = "Time taken to update task status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status) {
        
        logger.info("Updating task {} status to {}", taskId, status);
        
        try {
            Optional<TaskResponse> updatedTask = taskService.updateTaskStatus(taskId, status);
            
            if (updatedTask.isPresent()) {
                logger.info("Task {} status updated successfully", taskId);
                return ResponseEntity.ok(updatedTask.get());
            } else {
                logger.warn("Task not found for status update: {}", taskId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating task status: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel a task
     * 
     * POST /api/v1/tasks/{taskId}/cancel
     * 
     * @param taskId Task identifier
     * @return Success response
     */
    @PostMapping("/{taskId}/cancel")
    @Timed(value = "task.cancellation", description = "Time taken to cancel a task")
    public ResponseEntity<Map<String, String>> cancelTask(@PathVariable Long taskId) {
        logger.info("Cancelling task: {}", taskId);
        
        try {
            boolean cancelled = taskService.cancelTask(taskId);
            
            if (cancelled) {
                logger.info("Task {} cancelled successfully", taskId);
                return ResponseEntity.ok(Map.of("message", "Task cancelled successfully", "taskId", taskId.toString()));
            } else {
                logger.warn("Failed to cancel task: {}", taskId);
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to cancel task", "taskId", taskId.toString()));
            }
        } catch (Exception e) {
            logger.error("Error cancelling task: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retry a failed task
     * 
     * POST /api/v1/tasks/{taskId}/retry
     * 
     * @param taskId Task identifier
     * @return Success response
     */
    @PostMapping("/{taskId}/retry")
    @Timed(value = "task.retry", description = "Time taken to retry a task")
    public ResponseEntity<Map<String, String>> retryTask(@PathVariable Long taskId) {
        logger.info("Retrying task: {}", taskId);
        
        try {
            boolean retried = taskService.retryTask(taskId);
            
            if (retried) {
                logger.info("Task {} queued for retry", taskId);
                return ResponseEntity.ok(Map.of("message", "Task queued for retry", "taskId", taskId.toString()));
            } else {
                logger.warn("Failed to retry task: {}", taskId);
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to retry task", "taskId", taskId.toString()));
            }
        } catch (Exception e) {
            logger.error("Error retrying task: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get comprehensive task statistics
     * 
     * GET /api/v1/tasks/statistics
     * 
     * @return Task execution statistics and metrics
     */
    @GetMapping("/statistics")
    @Timed(value = "task.statistics", description = "Time taken to retrieve task statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        logger.debug("Retrieving task statistics");
        
        try {
            Map<String, Object> statistics = taskService.getTaskStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error retrieving task statistics: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get performance metrics
     * 
     * GET /api/v1/tasks/metrics
     * 
     * @param fromTime Start time for metrics (optional, defaults to last 24 hours)
     * @return Performance metrics including throughput, latency, and success rates
     */
    @GetMapping("/metrics")
    @Timed(value = "task.metrics", description = "Time taken to retrieve performance metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromTime) {
        
        logger.debug("Retrieving performance metrics from: {}", fromTime);
        
        try {
            LocalDateTime startTime = fromTime != null ? fromTime : LocalDateTime.now().minusDays(1);
            Map<String, Object> metrics = taskService.getPerformanceMetrics(startTime);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Error retrieving performance metrics: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get scheduled tasks
     * 
     * GET /api/v1/tasks/scheduled
     * 
     * @param beforeTime Get tasks scheduled before this time (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Paginated list of scheduled tasks
     */
    @GetMapping("/scheduled")
    @Timed(value = "task.scheduled", description = "Time taken to retrieve scheduled tasks")
    public ResponseEntity<Page<TaskResponse>> getScheduledTasks(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime beforeTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Retrieving scheduled tasks before: {}", beforeTime);
        
        try {
            LocalDateTime threshold = beforeTime != null ? beforeTime : LocalDateTime.now().plusDays(1);
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            
            Page<TaskResponse> scheduledTasks = taskService.getScheduledTasks(threshold, pageable);
            return ResponseEntity.ok(scheduledTasks);
        } catch (Exception e) {
            logger.error("Error retrieving scheduled tasks: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Administrative endpoint to clean up old completed tasks
     * 
     * DELETE /api/v1/tasks/cleanup
     * 
     * @param olderThan Delete tasks completed before this time (required)
     * @return Cleanup result with count of deleted tasks
     */
    @DeleteMapping("/cleanup")
    @Timed(value = "task.cleanup", description = "Time taken to cleanup old tasks")
    public ResponseEntity<Map<String, Object>> cleanupTasks(
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime olderThan) {
        
        logger.info("Cleaning up tasks older than: {}", olderThan);
        
        try {
            int deletedCount = taskService.cleanupCompletedTasks(olderThan);
            logger.info("Cleanup completed: {} tasks deleted", deletedCount);
            
            return ResponseEntity.ok(Map.of(
                "message", "Cleanup completed",
                "deletedCount", deletedCount,
                "olderThan", olderThan.toString()
            ));
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Health check endpoint
     * 
     * GET /api/v1/tasks/health
     * 
     * @return System health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "High-Throughput Task Scheduler",
                "version", "1.0.0"
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}