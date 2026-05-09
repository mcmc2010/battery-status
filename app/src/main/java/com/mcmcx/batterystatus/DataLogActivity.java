package com.mcmcx.batterystatus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mcmcx.batterystatus.data.model.BatteryLogEntry;
import com.mcmcx.batterystatus.data.model.DataLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataLogActivity extends AppCompatActivity {

    private ListView _listView;
    private LogAdapter _adapter;
    private final Handler _handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat _timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private final Runnable _refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing() && !isDestroyed()) {
                _adapter.notifyDataSetChanged();
                _handler.postDelayed(this, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BatteryStatus);
        setContentView(R.layout.activity_data_log);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        _listView = findViewById(R.id.list_log);
        _adapter = new LogAdapter();
        _listView.setAdapter(_adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _handler.post(_refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _handler.removeCallbacks(_refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _handler.removeCallbacks(_refreshRunnable);
    }

    private class LogAdapter extends BaseAdapter {

        private List<BatteryLogEntry> _entries;

        @Override
        public int getCount() {
            _entries = DataLogger.getInstance().getEntries();
            return _entries.size();
        }

        @Override
        public BatteryLogEntry getItem(int position) {
            return _entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_battery_log, parent, false);
            }

            TextView timeView = convertView.findViewById(R.id.log_time);
            TextView detailView = convertView.findViewById(R.id.log_detail);

            BatteryLogEntry entry = _entries.get(position);

            timeView.setText(_timeFormat.format(new Date(entry.timestamp)));

            String chargingInfo = entry.charging ? "Charging" : "Discharging";
            String detail = String.format(Locale.getDefault(),
                    "%s | %d%% | %.3fV | %.0fmA | %.1f°C | %dmAh",
                    chargingInfo,
                    Math.round(entry.percentage),
                    entry.voltage,
                    Math.abs(entry.current),
                    entry.temperature,
                    entry.capacity);
            detailView.setText(detail);

            return convertView;
        }
    }
}
