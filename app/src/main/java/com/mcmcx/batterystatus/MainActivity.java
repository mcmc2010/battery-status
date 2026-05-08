package com.mcmcx.batterystatus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.mcmcx.batterystatus.data.model.BatteryInfo;
import com.mcmcx.batterystatus.data.model.DataRecorder;
import com.mcmcx.batterystatus.util.BatteryUtils;
import com.mcmcx.batterystatus.util.RealTimeLineChart;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String BATTERY_STATUS_UPDATE = "com.mcmcx.batterystatus.BATTERY_STATUS_UPDATE";

    private DrawerLayout _drawerLayout;
    private NavigationView _navView;

    private ImageView _icon_status;
    private TextView _status_charging;
    private TextView _health_status;
    private TextView _status;

    private TextView _temperature;
    private TextView _voltage;
    private TextView _current;

    private TextView _power;
    private TextView _power_suffix;

    private TextView _capacity;

    private TextView _percentage;
    private TextView _view_timer;

    private CardView _cardVoltage;
    private CardView _cardCurrent;
    private CardView _cardTemperature;

    private RealTimeLineChart _chart;
    private DataRecorder _dataRecorder;
    private DataRecorder.Metric _selectedMetric = DataRecorder.Metric.TEMPERATURE;

    private Handler _handler;
    private Runnable _updateTimeRunnable;

    private boolean _is_charging = false;

    private long _timestamp_start = 0;
    private long _timestamp_update = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BatteryStatus);
        setContentView(R.layout.activity_main);

        _dataRecorder = new DataRecorder();

        _drawerLayout = findViewById(R.id.drawer_layout);
        _navView = findViewById(R.id.nav_view);
        _navView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, _drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        _icon_status = this.findViewById(R.id.id_icon_status);
        _status_charging = this.findViewById(R.id.charging_status);
        _health_status = this.findViewById(R.id.health_status);
        _status = this.findViewById(R.id.status);

        _temperature = findViewById(R.id.temperature);
        _voltage = findViewById(R.id.voltage);
        _current = findViewById(R.id.current);

        _power = this.findViewById(R.id.power);
        _power_suffix = this.findViewById(R.id.power_suffix);

        _capacity = this.findViewById(R.id.capacity);
        _view_timer = this.findViewById(R.id.timer);
        _percentage = this.findViewById(R.id.percentage);

        _cardVoltage = findViewById(R.id.card_voltage);
        _cardCurrent = findViewById(R.id.card_current);
        _cardTemperature = findViewById(R.id.card_temperature);

        _cardVoltage.setOnClickListener(v -> switchChart(DataRecorder.Metric.VOLTAGE));
        _cardCurrent.setOnClickListener(v -> switchChart(DataRecorder.Metric.CURRENT));
        _cardTemperature.setOnClickListener(v -> switchChart(DataRecorder.Metric.TEMPERATURE));

        _chart = findViewById(R.id.chart_temperature);

        _chart.setSeries(_dataRecorder.getSeries(_selectedMetric),
                getString(R.string.label_temperature),
                getString(R.string.unit_celsius), 0xFFE53935);
        updateCardSelection();

        _icon_status.setImageResource(R.drawable.ic_battery);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_NOT_EXPORTED);
            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE), RECEIVER_EXPORTED);
        } else {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE));
        }

        _handler = new Handler(Looper.getMainLooper());
        _updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                OnTimeUpdate();
                _handler.postDelayed(this, 1000);
            }
        };
        _handler.post(_updateTimeRunnable);

        _timestamp_start = System.currentTimeMillis();
    }

    private void switchChart(DataRecorder.Metric metric) {
        if (_selectedMetric == metric) return;
        _selectedMetric = metric;

        String label;
        String unit;
        int color;

        switch (metric) {
            case VOLTAGE:
                label = getString(R.string.label_voltage);
                unit = getString(R.string.unit_volt);
                color = 0xFF43A047;
                break;
            case CURRENT:
                label = getString(R.string.label_current);
                unit = getString(R.string.unit_milliampere);
                color = 0xFF1E88E5;
                break;
            case TEMPERATURE:
            default:
                label = getString(R.string.label_temperature);
                unit = getString(R.string.unit_celsius);
                color = 0xFFE53935;
                break;
        }

        _chart.setSeries(_dataRecorder.getSeries(metric), label, unit, color);
        updateCardSelection();
    }

    private void updateCardSelection() {
        float selectedElevation = dpToPx(6);
        float normalElevation = dpToPx(2);

        _cardVoltage.setCardElevation(
                _selectedMetric == DataRecorder.Metric.VOLTAGE ? selectedElevation : normalElevation);
        _cardCurrent.setCardElevation(
                _selectedMetric == DataRecorder.Metric.CURRENT ? selectedElevation : normalElevation);
        _cardTemperature.setCardElevation(
                _selectedMetric == DataRecorder.Metric.TEMPERATURE ? selectedElevation : normalElevation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            _drawerLayout.closeDrawers();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, R.string.nav_settings, Toast.LENGTH_SHORT).show();
            _drawerLayout.closeDrawers();
        } else if (id == R.id.nav_about) {
            Toast.makeText(this, R.string.nav_about, Toast.LENGTH_SHORT).show();
            _drawerLayout.closeDrawers();
        }

        return true;
    }

    private final BroadcastReceiver _batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                BatteryInfo info = new BatteryInfo();

                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                info.setStatus(status);
                info.setCharging(status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL);

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
                info.setPercentage((level / (float) scale) * 100f);

                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                info.setVoltage(voltage / 1000.0);

                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                info.setTemperature(temperature / 10.0f);

                info.setHealth(intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                        BatteryManager.BATTERY_HEALTH_UNKNOWN));
                info.setPlugged(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0));

                BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
                info.setCurrent(BatteryUtils.readCurrentMA(batteryManager));
                info.setCapacity(BatteryUtils.readCapacityMAh(batteryManager));

                Intent intentUpdate = new Intent(BATTERY_STATUS_UPDATE);
                intentUpdate.putExtra("status", info.getStatus());
                intentUpdate.putExtra("is_charging", info.isCharging());
                intentUpdate.putExtra("voltage", info.getVoltage());
                intentUpdate.putExtra("current", info.getCurrent());
                intentUpdate.putExtra("temperature", info.getTemperature());
                intentUpdate.putExtra("percentage", info.getPercentage());
                intentUpdate.putExtra("health", info.getHealth());
                intentUpdate.putExtra("plugged", info.getPlugged());
                intentUpdate.putExtra("capacity", info.getCapacity());

                context.sendBroadcast(intentUpdate);
            } catch (Exception e) {
                Log.e("batterystatus", e.getMessage());
            }
        }
    };

    protected void OnTimeUpdate() {
        if (_timestamp_start > 0) {
            long time = System.currentTimeMillis() - _timestamp_start;
            long seconds = (time / 1000) % 60;
            long minutes = ((time / 1000) / 60) % 60;
            _view_timer.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
        }

        if (_timestamp_update > 0) {
            long time = System.currentTimeMillis() - _timestamp_update;
            long seconds = (time / 1000) % 60;
            long minutes = ((time / 1000) / 60) % 60;
            _status.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _chart.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_batteryReceiver);
        unregisterReceiver(_batteryStatusUpdateReceiver);
        _handler.removeCallbacks(_updateTimeRunnable);
    }

    private final BroadcastReceiver _batteryStatusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _timestamp_update = System.currentTimeMillis();

            BatteryInfo info = new BatteryInfo();
            info.setStatus(intent.getIntExtra("status", 0));
            info.setCharging(intent.getBooleanExtra("is_charging", false));
            info.setVoltage(intent.getDoubleExtra("voltage", 0));
            info.setCurrent(intent.getDoubleExtra("current", 0));
            info.setTemperature(intent.getFloatExtra("temperature", 0.0f));
            info.setPercentage(intent.getFloatExtra("percentage", 0.0f));
            info.setHealth(intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN));
            info.setPlugged(intent.getIntExtra("plugged", 0));
            info.setCapacity(intent.getIntExtra("capacity", 0));

            _voltage.setText(String.format("%.3f", info.getVoltage()));
            _current.setText(String.format("%.1f", info.getAbsCurrent()));
            if (info.isCharging()) {
                _current.setTextColor(0xFF2E7D32);
            } else if (info.getCurrent() < 0) {
                _current.setTextColor(0xFFFF6D00);
            } else {
                _current.setTextColor(getColor(R.color.textSecondary));
            }
            _temperature.setText(String.format("%.1f", info.getTemperature()));
            _capacity.setText(String.format("%d", info.getCapacity()));
            _percentage.setText(String.format("%.0f%%", info.getPercentage()));

            _dataRecorder.recordVoltage((float) info.getVoltage());
            _dataRecorder.recordCurrent((float) info.getAbsCurrent());
            _dataRecorder.recordTemperature(info.getTemperature());

            if (_chart.getVisibility() == View.VISIBLE) {
                _chart.invalidate();
            }

            int healthResId = BatteryUtils.getHealthStringResId(info.getHealth());
            if (healthResId != 0) {
                _health_status.setText(healthResId);
                _health_status.setTextColor(BatteryUtils.getHealthColor(info.getHealth()));
            } else {
                _health_status.setText("");
            }

            if (info.isCharging()) {
                if (!_is_charging) {
                    _timestamp_start = System.currentTimeMillis();
                }
                _is_charging = true;

                _power.setText(String.format("%.2f", info.getPowerWatts()));
                _power_suffix.setText(R.string.unit_watt);

                _icon_status.setImageResource(R.drawable.ic_battery_charging);

                int pluggedResId = BatteryUtils.getPluggedStringResId(info.getPlugged());
                if (pluggedResId != 0) {
                    _status_charging.setText(getString(R.string.charging_format,
                            getString(pluggedResId)));
                } else {
                    _status_charging.setText(R.string.charging);
                }
            } else {
                if (_is_charging) {
                    _timestamp_start = System.currentTimeMillis();
                }
                _is_charging = false;

                _power.setText(String.format("%.1f", info.getDischargeRateMahPerMin()));
                _power_suffix.setText(R.string.unit_milliampere_hour_per_min);

                _icon_status.setImageResource(R.drawable.ic_battery);
                _status_charging.setText(R.string.using);
            }
        }
    };

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
