-- High-Throughput Task Scheduler - Tasks Table Creation
-- This script creates the main tasks table with optimized indexes for high-performance operations
-- Version: 1.0.0

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payload TEXT,
    description VARCHAR(500),
    scheduled_time TIMESTAMP WITHOUT TIME ZONE,
    next_execution_time TIMESTAMP WITHOUT TIME ZONE,
    execution_timeout_seconds INTEGER DEFAULT 300,
    max_retry_attempts INTEGER DEFAULT 3,
    current_retry_count INTEGER DEFAULT 0,
    last_error_message VARCHAR(1000),
    last_executed_at TIMESTAMP WITHOUT TIME ZONE,
    execution_duration_ms BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_priority CHECK (priority IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'BULK')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'RETRYING', 'PAUSED', 'SCHEDULED')),
    CONSTRAINT chk_timeout_positive CHECK (execution_timeout_seconds > 0),
    CONSTRAINT chk_retry_attempts_non_negative CHECK (max_retry_attempts >= 0),
    CONSTRAINT chk_current_retry_non_negative CHECK (current_retry_count >= 0),
    CONSTRAINT chk_execution_duration_non_negative CHECK (execution_duration_ms IS NULL OR execution_duration_ms >= 0)
);

-- High-performance indexes optimized for task scheduler operations
-- Primary index for priority queue operations (most critical for performance)
CREATE INDEX idx_task_status_priority_created 
ON tasks(status, priority DESC, created_at ASC) 
WHERE status IN ('PENDING', 'QUEUED');

-- Index for scheduled task retrieval
CREATE INDEX idx_task_next_execution 
ON tasks(next_execution_time ASC) 
WHERE next_execution_time IS NOT NULL;

-- Index for task monitoring and management
CREATE INDEX idx_task_created_at 
ON tasks(created_at DESC);

-- Index for status-based queries
CREATE INDEX idx_task_status 
ON tasks(status);

-- Index for task type filtering
CREATE INDEX idx_task_type_status 
ON tasks(task_type, status);

-- Index for retry management
CREATE INDEX idx_task_retry_eligible 
ON tasks(status, current_retry_count, max_retry_attempts, last_executed_at) 
WHERE status = 'FAILED';

-- Index for stuck task detection
CREATE INDEX idx_task_stuck_detection 
ON tasks(status, last_executed_at) 
WHERE status = 'RUNNING';

-- Index for performance metrics
CREATE INDEX idx_task_performance_metrics 
ON tasks(status, last_executed_at, execution_duration_ms) 
WHERE status = 'COMPLETED';

-- Partial index for active tasks (memory optimization)
CREATE INDEX idx_task_active 
ON tasks(id, status, priority DESC, created_at ASC) 
WHERE status NOT IN ('COMPLETED', 'CANCELLED');

-- Index for cleanup operations
CREATE INDEX idx_task_cleanup 
ON tasks(status, updated_at) 
WHERE status = 'COMPLETED';

-- Updated_at trigger for automatic timestamp management
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tasks_updated_at 
    BEFORE UPDATE ON tasks 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Priority enum mapping for consistent priority ordering
-- CRITICAL = 100, HIGH = 75, MEDIUM = 50, LOW = 25, BULK = 1
-- This ensures proper priority queue ordering in database queries

-- Comments for documentation
COMMENT ON TABLE tasks IS 'Main table storing all tasks for the high-throughput task scheduler';
COMMENT ON COLUMN tasks.id IS 'Unique task identifier (auto-incrementing)';
COMMENT ON COLUMN tasks.task_name IS 'Human-readable task name';
COMMENT ON COLUMN tasks.task_type IS 'Type/category of the task for processing logic';
COMMENT ON COLUMN tasks.priority IS 'Task priority level (CRITICAL, HIGH, MEDIUM, LOW, BULK)';
COMMENT ON COLUMN tasks.status IS 'Current task status in the execution lifecycle';
COMMENT ON COLUMN tasks.payload IS 'JSON or text payload containing task data';
COMMENT ON COLUMN tasks.next_execution_time IS 'When the task should be executed next';
COMMENT ON COLUMN tasks.execution_timeout_seconds IS 'Maximum execution time allowed';
COMMENT ON COLUMN tasks.max_retry_attempts IS 'Maximum number of retry attempts';
COMMENT ON COLUMN tasks.current_retry_count IS 'Current number of retry attempts made';
COMMENT ON COLUMN tasks.execution_duration_ms IS 'Actual execution time in milliseconds';
COMMENT ON COLUMN tasks.version IS 'Version for optimistic locking';

-- Create sequences for high-performance ID generation
-- The BIGSERIAL already creates a sequence, but we can optimize it
ALTER SEQUENCE tasks_id_seq CACHE 100;

-- Grant permissions (adjust as needed for your environment)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON tasks TO task_scheduler_app;
-- GRANT USAGE, SELECT ON SEQUENCE tasks_id_seq TO task_scheduler_app;