# BatteryStatus

Android 电池状态实时监控应用，显示电压、电流、温度、功率、容量等信息，并支持充电/放电计时。

## 当前状态

| 模块 | 说明 |
|------|------|
| 包名 | `com.mcmcx.batterystatus` |
| 语言 | Java |
| 构建 | Gradle 7.4.2 / AGP 7.4.2 |
| 最低 SDK | API 26 (Android 8.0) |
| 目标 SDK | API 34 |
| 版本 | 1.01 (versionCode 1004) |
| 架构 | 单 Activity (`MainActivity`) + BroadcastReceiver |

### 已实现功能

- [x] 电池电压 (V)
- [x] 电池电流 (mA)
- [x] 电池温度 (°C)
- [x] 充电功率 (W) / 放电速率 (mAh/m)
- [x] 剩余容量 (mAh)
- [x] 电池百分比显示
- [x] 充电/放电状态指示
- [x] 充电/放电时段计时
- [x] 数据刷新时间展示
- [x] CardView 网格布局 UI
- [x] 暗色模式适配 (DayNight)
- [x] 字符串资源国际化
- [x] ProGuard 代码混淆 (Release)
- [x] 电流数据容错（`CURRENT_NOW` → `CURRENT_AVERAGE` 降级）

### 已知问题

- [-] 无单元测试，（不处理）
- [ ] `CHARGE_COUNTER` 在某些设备上不可用（已容错为 0，但缺少提示）

---

## 开发规划

### 第一阶段：代码清理与 Bug 修复（~~v1.1~~ 已完成）

| 任务 | 优先级 | 状态 |
|------|--------|------|
| 修复图标颠倒 | P0 | ✅ 已完成 |
| 理清电流计算逻辑 | P0 | ✅ 已完成（增加 CURRENT_AVERAGE 降级） |
| 删除死代码 | P1 | ✅ 已完成 |
| 提取字符串资源 | P1 | ✅ 已完成 |
| 添加电池百分比展示 | P1 | ✅ 已完成 |
| 开启 ProGuard | P1 | ✅ 已完成 |
| 补全 contentDescription | P2 | ✅ 已完成 |
| 移除无效 BATTERY_STATS 权限 | P1 | ✅ 已完成 |
| 暗色模式适配 | P2 | ✅ 已完成（values-night/colors.xml） |
| Power 卡片补全标题 | P2 | ✅ 已完成 |

### 第二阶段：功能增强（v1.2）

**目标**：丰富数据维度，提升用户体验

| 任务 | 优先级 | 说明 |
|------|--------|------|
| 电池健康状态 | P0 | 显示 `EXTRA_HEALTH`（Good/Cold/Dead/Overheat/OverVoltage） |
| 充电技术检测 | P1 | 显示充电类型（USB/AC/Wireless/Dock） |
| 实时图表 | P1 | 使用 MPAndroidChart 绘制电压/电流/温度走势图（最近 5 分钟） |
| 电池历史记录 | P2 | 本地 SQLite 存储充放电历史（时间、时长、电量变化） |
| 设置页面 | P2 | PreferenceFragment 提供刷新频率、通知开关等选项 |
| 充放电通知 | P2 | 充满电提醒、低电量警告（Notification） |
| 电池估算时间 | P2 | 结合当前电流估算充满/放完的剩余时间 |

### 第三阶段：架构升级（v2.0）

**目标**：代码可维护性、可扩展性提升

| 任务 | 优先级 | 说明 |
|------|--------|------|
| MVVM 重构 | P0 | 引入 ViewModel + LiveData，解耦 UI 与数据逻辑 |
| Kotlin 迁移 | P1 | 逐步将 Java 代码迁移到 Kotlin |
| 后台服务 | P1 | `ForegroundService` 实现后台持续监控 + 通知栏常驻 |
| Widget | P2 | 桌面小部件，快速查看电池状态 |
| 单元测试 | P2 | JUnit + Mockito 覆盖核心数据逻辑 |
| CI/CD | P2 | GitHub Actions 自动构建 APK / Lint 检查 |
| 多语言 | P2 | 英文 + 中文 + 多语言 strings.xml |

---

## 项目结构（建议）

```
app/src/main/java/com/mcmcx/batterystatus/
├── MainActivity.java          # 主界面（后续拆分为 Activity + Fragment）
├── data/
│   ├── model/
│   │   └── BatteryInfo.java   # 电池数据模型
│   ├── repository/
│   │   └── BatteryRepository.java
│   └── local/
│       └── BatteryDatabase.java
├── service/
│   └── BatteryMonitorService.java
├── ui/
│   ├── chart/
│   ├── settings/
│   └── widget/
└── util/
    └── BatteryUtils.java
```

## 构建 & 运行

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```
