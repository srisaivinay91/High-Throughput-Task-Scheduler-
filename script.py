import os
import json

# Create the complete project structure
project_structure = {
    "project_name": "high-throughput-task-scheduler",
    "description": "Distributed High-Throughput Task Scheduler with Spring Boot",
    "structure": {
        "pom.xml": "Maven build file",
        "README.md": "Project documentation and setup guide",
        "docker-compose.yml": "Docker configuration for services",
        "Dockerfile": "Docker image configuration",
        "src/main/java/com/taskscheduler/": {
            "TaskSchedulerApplication.java": "Main Spring Boot application",
            "config/": {
                "AsyncConfig.java": "Async and thread pool configuration",
                "DatabaseConfig.java": "Database configuration",
                "RedisConfig.java": "Redis configuration for caching"
            },
            "controller/": {
                "TaskController.java": "REST API endpoints for task management"
            },
            "service/": {
                "TaskService.java": "Service interface",
                "TaskServiceImpl.java": "Task service implementation",
                "PriorityTaskScheduler.java": "Priority queue scheduler implementation",
                "WorkerManager.java": "Multi-worker management service"
            },
            "repository/": {
                "TaskRepository.java": "JPA repository for tasks",
                "TaskExecutionRepository.java": "Repository for execution tracking"
            },
            "model/": {
                "Task.java": "Task entity model",
                "TaskExecution.java": "Task execution tracking entity",
                "TaskStatus.java": "Task status enum",
                "Priority.java": "Priority enum"
            },
            "dto/": {
                "TaskRequest.java": "Task creation request DTO",
                "TaskResponse.java": "Task response DTO",
                "TaskStatusResponse.java": "Task status response DTO"
            },
            "worker/": {
                "TaskWorker.java": "Task worker implementation",
                "WorkerPool.java": "Worker pool management"
            },
            "exception/": {
                "TaskNotFoundException.java": "Custom exception",
                "TaskSchedulerException.java": "Base exception class"
            }
        },
        "src/main/resources/": {
            "application.yml": "Application configuration",
            "application-prod.yml": "Production configuration",
            "db/migration/": {
                "V1__create_tasks_table.sql": "Database migration script",
                "V2__create_task_execution_table.sql": "Execution tracking table"
            }
        },
        "src/test/java/com/taskscheduler/": {
            "TaskSchedulerApplicationTests.java": "Integration tests",
            "service/": {
                "TaskServiceTest.java": "Service unit tests"
            },
            "controller/": {
                "TaskControllerTest.java": "Controller tests"
            }
        }
    }
}

print("High-Throughput Task Scheduler Project Structure:")
print("=" * 60)
print(json.dumps(project_structure, indent=2))