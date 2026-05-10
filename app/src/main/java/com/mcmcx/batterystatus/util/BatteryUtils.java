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
     * 实测发现不同设备返回的原始值单位不统一（规范要求 µA，实际有 2mA/1mA 的情况），
     * 通过三段阈值判断并统一转换为 µA，最终 /1000 得到 mA：
     *   |raw| &lt; 2000  → 2mA 单位，转换 ×2×1000
     *   2000 ≤ |raw| &lt; 20000 → 1mA 单位，转换 ×1×1000
     *   |raw| ≥ 20000 → µA 单位（规范），不转换
     */
    public static double readCurrentMA(BatteryManager manager) {
        long raw = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        if (raw == Long.MIN_VALUE) {
            raw = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            if (raw == Long.MIN_VALUE) {
                return 0;
            }
        }

        // 处理单位不统一的情况（以下阈值基于实测校准）
        // 其他设备可能两者都返回 µA
        long absRaw = Math.abs(raw);
        // OnePlus 7 Pro (Android 13) CURRENT_NOW 返回 2mA 为单位的值
        if(absRaw < 2000) {
            raw = (long)(raw * 2.0 * 1000); // mA → µA
        }
        // 如果绝对值小于 20000
        // 单位是 1 mA，1000µA
        else if (absRaw < 20000) {
            raw = (long)(raw * 1.0 * 1000); // mA → µA
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
     * 估算充电器功率 (W)。
     * 双电芯检测：电压 &lt; 4.5V 视为双电芯，充电电压 ≈ 电池电压 × 2。
     * 单电芯直接用电池电压计算。
     */
    public static double estimateChargerWattage(double voltage, double currentMA, boolean isCharging) {
        if (!isCharging) return 0;

        double absCurrent = Math.abs(currentMA);
        if (absCurrent == 0) return 0;

        double chargerVoltage;

        if (voltage < 4.5) {
            chargerVoltage = voltage * 2.0;
        } else {
            chargerVoltage = voltage;
        }

        return chargerVoltage * absCurrent / 1000.0;
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
