package com.smartcampus.model;

import java.util.UUID;

/**
 * Represents a single sensor reading event.
 */
public class SensorReading {
    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(double value) {
        this();
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
