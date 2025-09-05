package com.taskscheduler.service;

import com.taskscheduler.dto.TaskRequest;
import com.taskscheduler.dto.TaskResponse;
import com.taskscheduler.exception.TaskNotFoundException;
import com.taskscheduler.model.Priority;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskStatus;
import com.taskscheduler.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PriorityTaskScheduler priorityTaskScheduler;

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = mapRequestToTask(taskRequest);
        Task savedTask = taskRepository.save(task);
        priorityTaskScheduler.enqueue(savedTask);
        return mapTaskToResponse(savedTask);
    }

    @Override
    public List<TaskResponse> createTasksBatch(List<TaskRequest> taskRequests) {
        List<Task> tasks = taskRequests.stream()
                .map(this::mapRequestToTask)
                .collect(Collectors.toList());
        
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        
        savedTasks.forEach(priorityTaskScheduler::enqueue);
        
        return savedTasks.stream()
                .map(this::mapTaskToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskResponse> getTask(Long taskId) {
        return taskRepository.findById(taskId).map(this::mapTaskToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(TaskStatus status, Priority priority, String taskType, Pageable pageable) {
        // This is a simplified implementation. A real-world scenario might use JPA Specifications for dynamic queries.
        if (status != null) {
            return taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(status, pageable).map(this::mapTaskToResponse);
        }
        return taskRepository.findAll(pageable).map(this::mapTaskToResponse);
    }

    @Override
    public Optional<TaskResponse> updateTaskStatus(Long taskId, TaskStatus newStatus) {
        return taskRepository.findById(taskId).map(task -> {
            if (TaskStatus.isValidTransition(task.getStatus(), newStatus)) {
                task.setStatus(newStatus);
                Task updatedTask = taskRepository.save(task);
                return mapTaskToResponse(updatedTask);
            }
            throw new IllegalStateException("Invalid status transition from " + task.getStatus() + " to " + newStatus);
        });
    }

    @Override
    public boolean cancelTask(Long taskId) {
        return taskRepository.findById(taskId).map(task -> {
            if (task.getStatus().isCancellable()) {
                task.setStatus(TaskStatus.CANCELLED);
                taskRepository.save(task);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public boolean retryTask(Long taskId) {
        return taskRepository.findById(taskId).map(task -> {
            if (task.getStatus().isRetriable() && task.canRetry()) {
                task.incrementRetryCount();
                task.setStatus(TaskStatus.QUEUED); // Re-queue for execution
                Task retriedTask = taskRepository.save(task);
                priorityTaskScheduler.enqueue(retriedTask);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStatistics() {
        return Map.of(
            "totalTasks", taskRepository.count(),
            "pendingTasks", taskRepository.countByStatus(TaskStatus.PENDING),
            "queuedTasks", taskRepository.countByStatus(TaskStatus.QUEUED),
            "runningTasks", taskRepository.countByStatus(TaskStatus.RUNNING),
            "completedTasks", taskRepository.countByStatus(TaskStatus.COMPLETED),
            "failedTasks", taskRepository.countByStatus(TaskStatus.FAILED)
        );
    }
    
    @Override
    public int cleanupCompletedTasks(LocalDateTime olderThan) {
        return taskRepository.deleteCompletedTasksOlderThan(olderThan);
    }

    // --- Other method implementations from the interface ---

    @Override
    public Page<TaskResponse> getScheduledTasks(LocalDateTime beforeTime, Pageable pageable) {
        return taskRepository.findTasksScheduledBefore(beforeTime, pageable).map(this::mapTaskToResponse);
    }

    @Override
    public boolean completeTask(Long taskId, Long executionDurationMs) {
        return taskRepository.findById(taskId).map(task -> {
            task.setStatus(TaskStatus.COMPLETED);
            task.setExecutionDurationMs(executionDurationMs);
            taskRepository.save(task);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean failTask(Long taskId, String errorMessage) {
        return taskRepository.findById(taskId).map(task -> {
            task.setStatus(TaskStatus.FAILED);
            task.setLastErrorMessage(errorMessage);
            taskRepository.save(task);
            return true;
        }).orElse(false);
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics(LocalDateTime fromTime) {
        Object[] metrics = taskRepository.getPerformanceMetrics(fromTime);
        if (metrics != null && metrics.length > 0 && metrics[0] != null) {
            return Map.of(
                "averageExecutionTimeMs", metrics[0],
                "minExecutionTimeMs", metrics[1],
                "maxExecutionTimeMs", metrics[2],
                "completedCount", metrics[3]
            );
        }
        return Map.of("message", "No completed tasks found in the time range.");
    }

    @Override
    public Page<TaskResponse> getStuckTasks(LocalDateTime stuckThreshold, Pageable pageable) {
        return taskRepository.findStuckTasks(stuckThreshold, pageable).map(this::mapTaskToResponse);
    }
    
    @Override
    public int resetStuckTasks(LocalDateTime stuckThreshold) {
        Page<Task> stuckTasks = taskRepository.findStuckTasks(stuckThreshold, Pageable.unpaged());
        if (!stuckTasks.isEmpty()) {
            List<Long> taskIds = stuckTasks.getContent().stream().map(Task::getId).collect(Collectors.toList());
            return taskRepository.updateTaskStatusBatch(taskIds, TaskStatus.PENDING);
        }
        return 0;
    }

    // --- Helper methods for mapping between DTO and Entity ---

    private Task mapRequestToTask(TaskRequest request) {
        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setPayload(request.getPayload());
        task.setDescription(request.getDescription());
        task.setScheduledTime(request.getScheduledTime());
        task.setNextExecutionTime(request.getScheduledTime() != null ? request.getScheduledTime() : LocalDateTime.now());
        task.setExecutionTimeoutSeconds(request.getExecutionTimeoutSeconds());
        task.setMaxRetryAttempts(request.getMaxRetryAttempts());
        task.setStatus(task.getScheduledTime() != null && task.getScheduledTime().isAfter(LocalDateTime.now()) ? TaskStatus.SCHEDULED : TaskStatus.PENDING);
        return task;
    }

    private TaskResponse mapTaskToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTaskName(task.getTaskName());
        response.setTaskType(task.getTaskType());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDescription(task.getDescription());
        response.setScheduledTime(task.getScheduledTime());
        response.setNextExecutionTime(task.getNextExecutionTime());
        response.setExecutionTimeoutSeconds(task.getExecutionTimeoutSeconds());
        response.setMaxRetryAttempts(task.getMaxRetryAttempts());
        response.setCurrentRetryCount(task.getCurrentRetryCount());
        response.setLastErrorMessage(task.getLastErrorMessage());
        response.setLastExecutedAt(task.getLastExecutedAt());
        response.setExecutionDurationMs(task.getExecutionDurationMs());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setVersion(task.getVersion());
        return response;
    }
}