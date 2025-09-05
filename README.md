# High-Throughput Task Scheduler

A distributed, high-performance task scheduling system built with **Java 17** and **Spring Boot 3**, designed to handle **10,000+ tasks per minute** with persistent priority queues, multi-worker architecture, and fault-tolerant mechanisms ensuring at-least-once task execution guarantees.

## 🚀 Key Features

- **High Throughput**: Processes 10,000+ tasks per minute
- **Persistent Priority Queue**: Advanced data structures with database-backed persistence
- **Multi-Worker Architecture**: Distributed processing with horizontal scaling
- **Fault Tolerance**: Circuit breakers, retries, and at-least-once execution guarantees
- **RESTful APIs**: Complete task submission and status tracking endpoints
- **Real-time Monitoring**: Comprehensive metrics with Prometheus and Grafana
- **Multi-level Caching**: In-memory, Redis, and PostgreSQL storage layers
- **Production Ready**: Docker containerization and orchestration

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │     API Layer   │    │  Priority Queue │
│     (Nginx)     │───▶│ (Spring Boot)   │───▶│   (In-Memory)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Monitoring    │    │   Worker Pool   │    │  Redis Cache    │
│ (Prometheus +   │◀───│  (Multi-thread) │───▶│  (Distributed)  │
│    Grafana)     │    └─────────────────┘    └─────────────────┘
└─────────────────┘             │                       │
                                 ▼                       ▼
                      ┌─────────────────┐    ┌─────────────────┐
                      │   PostgreSQL    │    │   Task Storage  │
                      │   (Persistent)  │◀───│  (Persistent)   │
                      └─────────────────┘    └─────────────────┘
```

### Multi-Level Storage Architecture:
- **Level 1**: In-memory PriorityBlockingQueue (fastest access, ~1ms latency)
- **Level 2**: Redis sorted sets (distributed cache, ~5ms latency)  
- **Level 3**: PostgreSQL database (persistent storage, ~10ms latency)

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+** 
- **Docker** and **Docker Compose** (for containerized deployment)
- **PostgreSQL 13+** (for persistence)
- **Redis 6+** (for caching)

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

1. **Clone and navigate to the project**:
   ```bash
   git clone <repository-url>
   cd high-throughput-task-scheduler
   ```

2. **Start all services**:
   ```bash
   docker-compose up -d
   ```

3. **Verify deployment**:
   ```bash
   curl http://localhost:8080/api/v1/tasks/health
   ```

4. **Access monitoring dashboards**:
   - **Application**: http://localhost:8080
   - **Grafana**: http://localhost:3000 (admin/admin123)
   - **Prometheus**: http://localhost:9090

### Option 2: Manual Setup

1. **Setup PostgreSQL**:
   ```sql
   CREATE DATABASE taskscheduler;
   CREATE USER taskscheduler WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE taskscheduler TO taskscheduler;
   ```

2. **Setup Redis**:
   ```bash
   redis-server --port 6379
   ```

3. **Build and run application**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## 📝 API Documentation

### Task Submission

**Create Single Task**
```bash
POST /api/v1/tasks
Content-Type: application/json

{
  "taskName": "Process User Data",
  "taskType": "DATA_PROCESSING",
  "priority": "HIGH",
  "payload": "{\"userId\": 12345, \"operation\": \"sync\"}",
  "description": "Synchronize user data with external system",
  "executionTimeoutSeconds": 300,
  "maxRetryAttempts": 3
}
```

**Response**:
```json
{
  "id": 1,
  "taskName": "Process User Data",
  "taskType": "DATA_PROCESSING",
  "priority": "HIGH",
  "status": "QUEUED",
  "createdAt": "2025-01-20T10:30:00",
  "nextExecutionTime": "2025-01-20T10:30:00"
}
```

**Batch Task Creation**
```bash
POST /api/v1/tasks/batch
Content-Type: application/json

[
  {
    "taskName": "Batch Job 1",
    "taskType": "BATCH_PROCESSING",
    "priority": "MEDIUM",
    "payload": "{\"batchId\": 1}"
  },
  {
    "taskName": "Batch Job 2", 
    "taskType": "BATCH_PROCESSING",
    "priority": "MEDIUM",
    "payload": "{\"batchId\": 2}"
  }
]
```

### Task Status Tracking

**Get Task Status**
```bash
GET /api/v1/tasks/{taskId}
```

**List Tasks with Filtering**
```bash
GET /api/v1/tasks?status=RUNNING&priority=HIGH&page=0&size=20&sort=createdAt,desc
```

**Update Task Status**
```bash
PUT /api/v1/tasks/{taskId}/status?status=COMPLETED
```

### Task Management

**Cancel Task**
```bash
POST /api/v1/tasks/{taskId}/cancel
```

**Retry Failed Task**
```bash
POST /api/v1/tasks/{taskId}/retry
```

### Monitoring & Statistics

**System Statistics**
```bash
GET /api/v1/tasks/statistics
```

**Performance Metrics**
```bash
GET /api/v1/tasks/metrics?fromTime=2025-01-20T00:00:00
```

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
# Thread Pool Configuration (High Throughput)
task-scheduler:
  thread-pool:
    core-pool-size: 20      # Base worker threads
    max-pool-size: 100      # Maximum worker threads
    queue-capacity: 10000   # Task queue capacity
    
  # Worker Configuration
  workers:
    initial-count: 10       # Initial worker instances
    max-count: 50          # Maximum worker instances
    
  # Priority Queue Configuration
  priority-queue:
    max-size: 100000       # Maximum queue size
    batch-size: 100        # Batch processing size
    poll-interval: 100     # Queue polling interval (ms)
```

### Performance Tuning

**For High Throughput (10,000+ tasks/minute)**:
```yaml
task-scheduler:
  thread-pool:
    core-pool-size: 30
    max-pool-size: 150
    queue-capacity: 15000
  workers:
    initial-count: 15
    max-count: 75
```

**Database Optimizations**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

## 🔧 Development

### Project Structure
```
src/
├── main/java/com/taskscheduler/
│   ├── TaskSchedulerApplication.java          # Main application
│   ├── config/
│   │   ├── AsyncConfig.java                   # Thread pool configuration
│   │   ├── DatabaseConfig.java                # Database configuration  
│   │   └── RedisConfig.java                   # Redis configuration
│   ├── controller/
│   │   └── TaskController.java                # REST API endpoints
│   ├── service/
│   │   ├── TaskService.java                   # Service interface
│   │   ├── TaskServiceImpl.java               # Service implementation
│   │   ├── PriorityTaskScheduler.java         # Priority queue service
│   │   └── WorkerManager.java                 # Worker management
│   ├── repository/
│   │   └── TaskRepository.java                # Data access layer
│   ├── model/
│   │   ├── Task.java                          # Task entity
│   │   ├── Priority.java                      # Priority enum
│   │   └── TaskStatus.java                    # Status enum
│   ├── dto/
│   │   ├── TaskRequest.java                   # Request DTO
│   │   └── TaskResponse.java                  # Response DTO
│   └── worker/
│       ├── TaskWorker.java                    # Task execution worker
│       └── WorkerPool.java                    # Worker pool management
└── main/resources/
    ├── application.yml                        # Main configuration
    ├── application-prod.yml                   # Production config
    └── db/migration/
        └── V1__create_tasks_table.sql         # Database schema
```

### Building from Source

```bash
# Clean build
./mvnw clean compile

# Run tests
./mvnw test

# Package application
./mvnw clean package

# Build Docker image
docker build -t taskscheduler:latest .

# Run with custom profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Migrations

Database schema is managed with Flyway:

```bash
# Apply migrations
./mvnw flyway:migrate

# Check migration status  
./mvnw flyway:info

# Reset database (development only)
./mvnw flyway:clean flyway:migrate
```

## 📊 Monitoring & Observability

### Metrics Available

- **Throughput Metrics**: Tasks/minute, completion rates
- **Performance Metrics**: Execution times, queue latencies  
- **System Metrics**: Thread pool utilization, memory usage
- **Business Metrics**: Priority distribution, retry rates

### Grafana Dashboards

Pre-configured dashboards available:
- **Task Scheduler Overview**: High-level system metrics
- **Performance Dashboard**: Detailed performance analysis
- **Infrastructure Metrics**: Database and Redis monitoring

### Health Checks

Multiple health check endpoints:
```bash
# Application health
GET /actuator/health

# Detailed health with components  
GET /actuator/health/detailed

# Custom task scheduler health
GET /api/v1/tasks/health
```

## 🧪 Testing

### Load Testing Example

Test high-throughput capabilities:

```bash
# Install wrk (load testing tool)
# Ubuntu: apt-get install wrk
# macOS: brew install wrk

# Test single task creation
wrk -t12 -c100 -d30s -s scripts/create-task.lua http://localhost:8080/api/v1/tasks

# Test batch task creation  
wrk -t12 -c100 -d30s -s scripts/batch-tasks.lua http://localhost:8080/api/v1/tasks/batch
```

Example `scripts/create-task.lua`:
```lua
wrk.method = "POST"
wrk.body = '{"taskName":"Load Test Task","taskType":"LOAD_TEST","priority":"MEDIUM","payload":"test data"}'
wrk.headers["Content-Type"] = "application/json"
```

### Performance Benchmarks

Target performance metrics:
- **Throughput**: 10,000+ tasks/minute (167+ tasks/second)
- **Latency**: <100ms average API response time
- **Queue Processing**: <50ms average dequeue time
- **Memory Usage**: <4GB under full load

## 🚀 Production Deployment

### Environment Variables

Required environment variables for production:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskscheduler
SPRING_DATASOURCE_USERNAME=taskscheduler  
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Redis
SPRING_REDIS_HOST=redis-cluster.example.com
SPRING_REDIS_PASSWORD=your_redis_password

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Performance Tuning
TASK_SCHEDULER_THREAD_POOL_CORE_POOL_SIZE=50
TASK_SCHEDULER_THREAD_POOL_MAX_POOL_SIZE=200
TASK_SCHEDULER_WORKERS_MAX_COUNT=100

# JVM Optimization  
JAVA_OPTS="-Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Kubernetes Deployment

Example Kubernetes manifest:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: taskscheduler-app
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: taskscheduler
        image: taskscheduler:1.0.0
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
```

### Scaling Guidelines

**Horizontal Scaling**:
- Deploy multiple application instances behind load balancer
- Use Redis for distributed queue coordination
- Configure database connection pooling appropriately

**Vertical Scaling**: 
- Increase JVM heap size: `-Xmx8g` or higher
- Increase thread pool sizes proportionally
- Monitor memory usage and GC performance

## 🔒 Security Considerations

- **Database**: Use encrypted connections (SSL/TLS)
- **Redis**: Enable AUTH and use Redis ACLs  
- **API**: Implement authentication/authorization (not included in base version)
- **Containers**: Run as non-root user, scan images for vulnerabilities
- **Network**: Use private networks for service communication

## 📚 Additional Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Performance Tuning](https://www.postgresql.org/docs/current/performance-tips.html)
- [Redis Best Practices](https://redis.io/topics/memory-optimization)

### Monitoring Tools
- [Prometheus](https://prometheus.io/docs/)
- [Grafana](https://grafana.com/docs/)
- [Micrometer](https://micrometer.io/docs)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`  
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request


## 🆘 Support

For questions and support:
- **Issues**: Create GitHub issues for bugs and feature requests
- **Documentation**: Check the `/docs` folder for detailed documentation
- **Monitoring**: Use Grafana dashboards for system observability

---

**Built for high-performance distributed task processing**
