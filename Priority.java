package com.taskscheduler.model;

/**
 * Priority enum for task scheduling
 * 
 * Defines the priority levels for tasks in the distributed queue system.
 * Higher numeric values represent higher priority for execution ordering.
 * 
 * Priority levels are designed for:
 * - CRITICAL: System-critical tasks that must execute immediately
 * - HIGH: Important business tasks requiring prompt execution
 * - MEDIUM: Standard business tasks with normal scheduling
 * - LOW: Background tasks that can be deferred
 * - BULK: Batch processing tasks with lowest priority
 */
public enum Priority {
    CRITICAL(100, "Critical priority - execute immediately"),
    HIGH(75, "High priority - execute as soon as possible"),
    MEDIUM(50, "Medium priority - normal execution order"),
    LOW(25, "Low priority - can be deferred"),
    BULK(1, "Bulk priority - batch processing");

    private final int value;
    private final String description;

    Priority(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Compare priorities for queue ordering
     * 
     * @param other Priority to compare against
     * @return negative if this priority is lower, positive if higher, 0 if equal
     */
    public int compareTo(Priority other) {
        return Integer.compare(this.value, other.value);
    }

    /**
     * Check if this priority is higher than another
     * 
     * @param other Priority to compare against
     * @return true if this priority is higher
     */
    public boolean isHigherThan(Priority other) {
        return this.value > other.value;
    }

    /**
     * Get priority from string value with case-insensitive matching
     * 
     * @param value String representation of priority
     * @return Priority enum value
     * @throws IllegalArgumentException if value doesn't match any priority
     */
    public static Priority fromString(String value) {
        if (value == null) {
            return MEDIUM; // Default priority
        }
        
        try {
            return Priority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority value: " + value + 
                ". Valid values are: CRITICAL, HIGH, MEDIUM, LOW, BULK");
        }
    }

    @Override
    public String toString() {
        return name() + " (" + value + ") - " + description;
    }
}