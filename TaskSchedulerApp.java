package com.taskscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * High-Throughput Task Scheduler Application
 * 
 * A distributed task scheduling system that provides:
 * - High-throughput task processing (10,000+ tasks/minute)
 * - Persistent priority queue with advanced data structures
 * - Multi-worker architecture with fault-tolerant mechanisms
 * - At-least-once task execution guarantees
 * - RESTful APIs for task submission and status tracking
 * 
 * @author Task Scheduler Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class TaskSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskSchedulerApplication.class, args);
    }
}