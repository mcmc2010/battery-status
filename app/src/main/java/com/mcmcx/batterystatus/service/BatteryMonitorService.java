package com.mcmcx.batterystatus.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mcmcx.batterystatus.MainActivity;
import com.mcmcx.batterystatus.R;
import com.mcmcx.batterystatus.data.model.BatteryInfo;
import com.mcmcx.batterystatus.data.model.BatteryLogEntry;
import com.mcmcx.batterystatus.data.model.DataLogger;
import com.mcmcx.batterystatus.data.model.DataRecorder;
import com.mcmcx.batterystatus.util.BatteryUtils;

import java.util.Locale;

public class BatteryMonitorService extends Service {

    public static final String BATTERY_STATUS_UPDATE = "com.mcmcx.batterystatus.BATTERY_STATUS_UPDATE";
    private static final String CHANNEL_ID = "battery_monitor";
    private static final int NOTIFICATION_ID = 1;

    private final DataRecorder _dataRecorder = DataRecorder.getInstance();
    private BroadcastReceiver _batteryReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        _batteryReceiver = new BroadcastReceiver() {
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

                    DataLogger.getInstance().add(new BatteryLogEntry(
                            System.currentTimeMillis(), status, info.isCharging(),
                            level, scale, info.getPercentage(),
                            info.getVoltage(), info.getCurrent(), info.getTemperature(),
                            info.getHealth(), info.getPlugged(), info.getCapacity()));

                    _dataRecorder.recordVoltage((float) info.getVoltage());
                    _dataRecorder.recordCurrent((float) info.getAbsCurrent());
                    _dataRecorder.recordTemperature(info.getTemperature());

                    updateNotification(info);

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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.sendBroadcast(intentUpdate);
                    } else {
                        context.sendBroadcast(intentUpdate);
                    }
                } catch (Exception e) {
                    Log.e("BatteryService", e.getMessage());
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(_batteryReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(_batteryReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification("Starting..."));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_batteryReceiver);
    }

    private void updateNotification(BatteryInfo info) {
        String content;
        if (info.isCharging()) {
            double wattage = BatteryUtils.estimateChargerWattage(
                    info.getVoltage(), info.getCurrent(), true);
            content = String.format(Locale.getDefault(),
                    "Charging · %.0fW | %.0f%%", wattage, info.getPercentage());
        } else {
            content = String.format(Locale.getDefault(),
                    "Discharging | %.0f%%", info.getPercentage());
        }

        Notification notification = buildNotification(content);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification(String content) {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.channel_desc));
        channel.setShowBadge(false);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
    }
}
