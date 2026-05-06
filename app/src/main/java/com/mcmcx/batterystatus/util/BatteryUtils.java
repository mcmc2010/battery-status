package com.mcmcx.batterystatus.util;

import android.content.Context;
import android.os.BatteryManager;

import androidx.annotation.ColorInt;

import com.mcmcx.batterystatus.R;

public class BatteryUtils {

    private BatteryUtils() {
    }

    /**
     * 读取瞬时电流 (mA)。
     * 优先使用 CURRENT_NOW，不可用时降级到 CURRENT_AVERAGE，再不可用返回 0。
     * 部分设备返回的原始值单位不统一（规范要求 µA，实际有 mA 的情况），
     * 通过阈值判断：|raw| &lt; 1000 视为已是 mA，否则从 µA 转换。
     */
    public static double readCurrentMA(BatteryManager manager) {
        long raw = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        if (raw == Long.MIN_VALUE) {
            raw = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            if (raw == Long.MIN_VALUE) {
                return 0;
            }
        }

        long absRaw = Math.abs(raw);
        if (absRaw < 1000) {
            return raw; // already in mA
        }
        return raw / 1000.0; // µA → mA
    }

    /**
     * 读取剩余容量 (mAh)。
     * BATTERY_PROPERTY_CHARGE_COUNTER 返回微安时 (µAh)，除以 1000 转换为毫安时。
     * 不可用时返回 0。
     */
    public static int readCapacityMAh(BatteryManager manager) {
        int capacity = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        if (capacity == Integer.MIN_VALUE) {
            return 0;
        }
        return capacity / 1000;
    }

    /**
     * 将插电类型常量转为本地化字符串资源 ID。
     */
    public static int getPluggedStringResId(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return R.string.plugged_ac;
            case BatteryManager.BATTERY_PLUGGED_USB:
                return R.string.plugged_usb;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return R.string.plugged_wireless;
            case BatteryManager.BATTERY_PLUGGED_DOCK:
                return R.string.plugged_dock;
            default:
                return 0;
        }
    }

    /**
     * 将 BatteryManager 健康状态常量转为本地化字符串资源 ID。
     */
    public static int getHealthStringResId(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return R.string.health_good;
            case BatteryManager.BATTERY_HEALTH_COLD:
                return R.string.health_cold;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return R.string.health_dead;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return R.string.health_overheat;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return R.string.health_over_voltage;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return R.string.health_failure;
            default:
                return 0;
        }
    }

    /**
     * 将 BatteryManager 健康状态常量转为对应颜色值。
     */
    @ColorInt
    public static int getHealthColor(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return 0xFF81C784;
            case BatteryManager.BATTERY_HEALTH_COLD:
                return 0xFF64B5F6;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return 0xFFE57373;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return 0xFFFFB74D;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return 0xFFFF8A65;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return 0xFFE57373;
            default:
                return 0xFFFFFFFF;
        }
    }
}
