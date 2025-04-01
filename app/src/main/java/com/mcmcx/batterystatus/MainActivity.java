package com.mcmcx.batterystatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;


////
//
////
public class MainActivity extends AppCompatActivity {

    //
    public static final String BATTERY_STATUS_UPDATE = "com.mcmcx.batterystatus.BATTERY_STATUS_UPDATE";

    private TextView _chargingStatus;
    private TextView _temperature;
    private TextView _power;
    private TextView _voltage;
    private TextView _current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BatteryStatus);
        setContentView(R.layout.activity_main);

        // 初始化视图
        _chargingStatus = findViewById(R.id.charging_status);
        _temperature = findViewById(R.id.temperature);
        _power = findViewById(R.id.power);
        _voltage = findViewById(R.id.voltage);
        _current = findViewById(R.id.current);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_NOT_EXPORTED);

            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE), RECEIVER_EXPORTED);
        }
        else {
            registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            registerReceiver(_batteryStatusUpdateReceiver, new IntentFilter(BATTERY_STATUS_UPDATE));
        }
    }

    ////
    private final BroadcastReceiver _batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0); // 单位: mV
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0); // 单位: 0.1°C
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                boolean is_charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

                //
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

                int currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW); // 单位: 微安培
                int chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                double chargeMAH = chargeCounter / 1000f;
                double voltageV = voltage / 1000.0; // 转换为 V
                double currentA = currentNow / 1_000_000f; // 转换为 A
                double powerW = voltageV * currentA; // 计算功率
                double tempC = temperature / 10.0; // 转换为 °C

                Intent intentUpdate = new Intent(BATTERY_STATUS_UPDATE);
                intentUpdate.putExtra("is_charging", is_charging);
                intentUpdate.putExtra("voltage", voltageV);
                intentUpdate.putExtra("current", currentA);
                intentUpdate.putExtra("temperature", tempC);
                intentUpdate.putExtra("power", powerW);
                intentUpdate.putExtra("mah", chargeMAH);
                intentUpdate.putExtra("status", status);

                context.sendBroadcast(intentUpdate);
            }
            catch(Exception e) {
                Log.e("batterystatus", e.getMessage());
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_batteryReceiver);
        unregisterReceiver(_batteryStatusUpdateReceiver);
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    private final BroadcastReceiver _batteryStatusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean is_charging = intent.getBooleanExtra("is_charging", false);
            double voltage = intent.getDoubleExtra("voltage", 0);
            double current = intent.getDoubleExtra("current", 0);
            double temperature = intent.getDoubleExtra("temperature", 0);
            double mah = intent.getDoubleExtra("mah", 0);
            double power = intent.getDoubleExtra("power", 0);

            //Log.i("batterystatus", "Receive " + voltage + "V, " + current + "A");
            double absolute = Math.abs(current) * 1000f; // A → mA
            mah = absolute * 60;

            _chargingStatus.setText(is_charging ? "Charging" : "Using");
            _voltage.setText(String.format("%.3f",voltage));
            _current.setText(String.format("%.3f", absolute));
            _temperature.setText(String.format("%.1f", temperature));

            if(is_charging) {
                _power.setText(String.format("%.3f mAh", mah));
            }
            else {
                _power.setText(String.format("%.2f W", power));
            }
        }
    };
}