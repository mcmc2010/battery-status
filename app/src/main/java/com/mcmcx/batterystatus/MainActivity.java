package com.mcmcx.batterystatus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


////
//
////
public class MainActivity extends AppCompatActivity {

    //
    public static final String BATTERY_STATUS_UPDATE = "com.mcmcx.batterystatus.BATTERY_STATUS_UPDATE";

    private ImageView _icon_status;
    private TextView _status_charging;
    private TextView _status;

    private TextView _temperature;
    private TextView _voltage;
    private TextView _current;

    private TextView _power;
    private TextView _power_suffix;

    private TextView _capacity;

    private TextView _view_timer;

    //
    private Handler _handler;
    private Runnable _updateTimeRunnable;

    //
    private boolean _is_charging = false;

    private long _timestamp_start = 0; // 开始时间戳（单位：毫秒）
    private long _timestamp_update = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BatteryStatus);
        setContentView(R.layout.activity_main);

        // 初始化视图
        _icon_status = this.findViewById(R.id.id_icon_status);
        _status_charging = this.findViewById(R.id.charging_status);
        _status = this.findViewById(R.id.status);

        _temperature = findViewById(R.id.temperature);

        _voltage = findViewById(R.id.voltage);
        _current = findViewById(R.id.current);

        _power = this.findViewById(R.id.power);
        _power_suffix = this.findViewById(R.id.power_suffix);

        _capacity = this.findViewById(R.id.capacity);

        _view_timer = this.findViewById(R.id.timer);

        //
        _icon_status.setImageResource(R.drawable.ic_battery);
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_NOT_EXPORTED);

            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE), RECEIVER_EXPORTED);
        }
        else {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE));
        }


        // 初始化 Handler 和 Runnable
        _handler = new Handler(Looper.getMainLooper());
        _updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                OnTimeUpdate();
                _handler.postDelayed(this, 1000); // 每隔 1 秒执行一次
            }
        };
        _handler.post(_updateTimeRunnable);

        //
        _timestamp_start = System.currentTimeMillis();
    }

    ////
    private final BroadcastReceiver _batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
                // 计算百分比（带一位小数）
                float percentage = (level / (float)scale) * 100f;

                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0); // 单位: mV
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0); // 单位: 0.1°C


                boolean is_charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

                //
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

                long current = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW); // 单位: 微安培

                double voltageV = voltage / 1000.0; // 转换为 V
                double currentMA = current / 1000.0; // 转换为 mA
                float temperatureC = temperature / 10.0f; // 转换为 °C

                int capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                double remaining_capacity = capacity / 1000f;

                Intent intentUpdate = new Intent(BATTERY_STATUS_UPDATE);
                intentUpdate.putExtra("status", status);
                intentUpdate.putExtra("is_charging", is_charging);

                intentUpdate.putExtra("voltage", voltageV);
                intentUpdate.putExtra("current", currentMA);

                intentUpdate.putExtra("temperature", temperatureC);

                intentUpdate.putExtra("percentage", percentage);
                intentUpdate.putExtra("capacity", (int)remaining_capacity);

                context.sendBroadcast(intentUpdate);
            }
            catch(Exception e) {
                Log.e("batterystatus", e.getMessage());
            }
        }
    };

    protected void OnTimeUpdate() {

        // 开始充电或放电时间
        if(_timestamp_start > 0)
        {
            long time = System.currentTimeMillis() - _timestamp_start;
            long seconds = (time / 1000) % 60;
            long minutes = ((time / 1000) / 60) % 60;
            _view_timer.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
        }

        // 数据刷新时间
        if(_timestamp_update > 0)
        {
            long time = System.currentTimeMillis() - _timestamp_update;
            long seconds = (time / 1000) % 60;
            long minutes = ((time / 1000) / 60) % 60;
            _status.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //
        unregisterReceiver(_batteryReceiver);
        unregisterReceiver(_batteryStatusUpdateReceiver);

        //
        _handler.removeCallbacks(_updateTimeRunnable);
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    private final BroadcastReceiver _batteryStatusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //
            // 设置开始时间戳（示例：当前时间）
            _timestamp_update = System.currentTimeMillis();

            //
            int status = intent.getIntExtra("status", 0);
            boolean is_charging = intent.getBooleanExtra("is_charging", false);

            double voltage = intent.getDoubleExtra("voltage", 0); //V
            double current = intent.getDoubleExtra("current", 0); //mA

            float temperature = intent.getFloatExtra("temperature", 0.0f);

            // 按每秒计算，这里要转换为mA
            // 每个设备不一样,这里以20mA计算相当于5v就是360w，4v就是228w
            // 目前手机充电不太可能有这么大的充电
            double milliampere = Math.abs(current);
            if(milliampere < 20.0) {
                milliampere = milliampere * 60.0 * 60.0; // mA
            }

            int capacity = intent.getIntExtra("capacity", 0);

            _voltage.setText(String.format("%.3f",voltage));
            _current.setText(String.format("%.1f", milliampere));
            _temperature.setText(String.format("%.1f", temperature));

            _capacity.setText(String.format("%d", capacity));
            
            //Toast.makeText(MainActivity.this, String.format("%f", current), Toast.LENGTH_LONG).show();

            if(is_charging) {
                if(!_is_charging) {
                    _timestamp_start = System.currentTimeMillis();
                }
                _is_charging = true;

                double power = milliampere / 1000.0 * voltage; // W

                _power.setText(String.format("%.2f", power));
                _power_suffix.setText("W");

                _icon_status.setImageResource(R.drawable.ic_battery);
                _status_charging.setText("Charging");
            } else {
                if(_is_charging) {
                    _timestamp_start = System.currentTimeMillis();
                }
                _is_charging = false;

                // 每分钟放电mAh
                double amperehour = milliampere / 60.0; // mAh

                _power.setText(String.format("%.1f", amperehour));
                _power_suffix.setText("mAh/m");

                _icon_status.setImageResource(R.drawable.ic_battery_charging);
                _status_charging.setText("Using");
            }

        }
    };
}