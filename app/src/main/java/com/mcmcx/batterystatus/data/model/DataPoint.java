package com.mcmcx.batterystatus.data.model;

public class DataPoint {
    public final long timestamp;
    public final float value;

    public DataPoint(long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
