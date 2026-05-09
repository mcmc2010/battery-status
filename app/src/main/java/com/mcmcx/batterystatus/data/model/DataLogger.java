package com.mcmcx.batterystatus.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataLogger {

    private static final DataLogger INSTANCE = new DataLogger();
    private static final long WINDOW_MS = 30 * 60 * 1000;

    private final List<BatteryLogEntry> _entries = new ArrayList<>();

    private DataLogger() {
    }

    public static DataLogger getInstance() {
        return INSTANCE;
    }

    public void add(BatteryLogEntry entry) {
        _entries.add(entry);
        pruneOld();
    }

    public List<BatteryLogEntry> getEntries() {
        pruneOld();
        return Collections.unmodifiableList(_entries);
    }

    private void pruneOld() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        _entries.removeIf(e -> e.timestamp < cutoff);
    }
}
