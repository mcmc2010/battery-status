package com.mcmcx.batterystatus.data.model;

public class BatteryLogEntry {
    public final long timestamp;
    public final int status;
    public final boolean charging;
    public final int level;
    public final int scale;
    public final float percentage;
    public final double voltage;
    public final double current;
    public final float temperature;
    public final int health;
    public final int plugged;
    public final int capacity;

    public BatteryLogEntry(long timestamp, int status, boolean charging,
                           int level, int scale, float percentage,
                           double voltage, double current, float temperature,
                           int health, int plugged, int capacity) {
        this.timestamp = timestamp;
        this.status = status;
        this.charging = charging;
        this.level = level;
        this.scale = scale;
        this.percentage = percentage;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.health = health;
        this.plugged = plugged;
        this.capacity = capacity;
    }
}
