package com.mcmcx.batterystatus.data.model;

/**
 * 电池状态数据模型，封装所有电池相关数据。
 */
public class BatteryInfo {

    private int status;
    private boolean charging;
    private double voltage;
    private double current;
    private float temperature;
    private float percentage;
    private int health;
    private int capacity;
    private int plugged;

    public BatteryInfo() {
    }

    // ---- getters ----

    public int getStatus() {
        return status;
    }

    public boolean isCharging() {
        return charging;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getPercentage() {
        return percentage;
    }

    public int getHealth() {
        return health;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getPlugged() {
        return plugged;
    }

    // ---- setters ----

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPlugged(int plugged) {
        this.plugged = plugged;
    }

    // ---- computed ----

    /** 电流绝对值 (mA) */
    public double getAbsCurrent() {
        return Math.abs(current);
    }

    /** 充电功率 (W) */
    public double getPowerWatts() {
        return getAbsCurrent() / 1000.0 * voltage;
    }

    /** 放电速率 (mAh/m) */
    public double getDischargeRateMahPerMin() {
        return getAbsCurrent() / 60.0;
    }
}
