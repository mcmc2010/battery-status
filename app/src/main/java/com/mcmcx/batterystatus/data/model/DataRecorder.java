package com.mcmcx.batterystatus.data.model;

import java.util.LinkedList;

public class DataRecorder {

    public enum Metric { VOLTAGE, CURRENT, TEMPERATURE }

    private static final int MAX_POINTS = 300;

    private final LinkedList<DataPoint> _voltageData = new LinkedList<>();
    private final LinkedList<DataPoint> _currentData = new LinkedList<>();
    private final LinkedList<DataPoint> _temperatureData = new LinkedList<>();

    public void recordVoltage(float value) {
        record(value, _voltageData);
    }

    public void recordCurrent(float value) {
        record(value, _currentData);
    }

    public void recordTemperature(float value) {
        record(value, _temperatureData);
    }

    public LinkedList<DataPoint> getSeries(Metric metric) {
        switch (metric) {
            case VOLTAGE:
                return _voltageData;
            case CURRENT:
                return _currentData;
            case TEMPERATURE:
                return _temperatureData;
            default:
                return _temperatureData;
        }
    }

    private void record(float value, LinkedList<DataPoint> series) {
        series.addLast(new DataPoint(System.currentTimeMillis(), value));
        if (series.size() > MAX_POINTS) {
            series.removeFirst();
        }
    }
}
