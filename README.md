# BatteryStatus

Android 电池状态实时监控应用，显示电压、电流、温度、功率、容量、百分比、健康状态等信息，支持充电/放电计时和暗色模式。

## 项目信息

| 模块 | 说明 |
|------|------|
| 包名 | `com.mcmcx.batterystatus` |
| 语言 | Java |
| 构建 | Gradle 7.4.2 / AGP 7.4.2 |
| 最低 SDK | API 26 (Android 8.0) |
| 目标 SDK | API 34 |
| 版本 | 1.01 (versionCode 1004) |
| 架构 | Activity + BroadcastReceiver + Model + Util |

## 项目结构

```
app/src/main/java/com/mcmcx/batterystatus/
├── MainActivity.java              # 主界面（DrawerLayout + NavigationView）
├── data/
│   └── model/
│       ├── BatteryInfo.java       # 电池数据模型
│       ├── DataPoint.java         # 数据点模型（时间戳+值）
│       └── DataRecorder.java      # 数据记录器（三组数据并行记录）
└── util/
    ├── BatteryUtils.java          # 工具类（电流读取、健康状态映射）
    └── RealTimeLineChart.java     # 实时折线图自定义 View（支持数据源切换）

app/src/main/res/
├── layout/
│   ├── activity_main.xml          # 主布局（DrawerLayout + Toolbar + cards）
│   └── nav_header.xml             # 导航菜单头部
├── menu/
│   └── navigation_menu.xml        # 导航菜单项（Home/Settings/About）
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── styles.xml
└── values-night/
    └── colors.xml                 # 暗色模式颜色
```

## 功能

### 核心监控
- 电池电压 / 电流 / 温度
- 充电功率 (W) / 放电速率 (mAh/m)
- 充电技术检测（AC/USB/Wireless/Dock）
- 剩余容量 (mAh) / 电池百分比
- 电池健康状态（含颜色指示）
- 充电/放电状态 + 时段计时
- 实时图表（电压/电流/温度，点击卡片切换折线图）

### 工程特性
- 暗色模式适配 (DayNight)
- 字符串资源国际化
- ProGuard 代码混淆 (Release)
- 电流容错降级 (`CURRENT_NOW` → `CURRENT_AVERAGE`)
- 电流单位自适应（阈值检测 mA/µA）

## 注意事项

- **电流单位检测** (`BatteryUtils.readCurrentMA`)：`BATTERY_PROPERTY_CURRENT_NOW` 规范要求返回 µA，但不同厂商实现不一致。通过三段阈值判断原始值单位：
  - `|raw| < 2000` → 10mA 单位（如 OnePlus 7 Pro），转换 `×10×1000`
  - `2000 ≤ |raw| < 20000` → 1mA 单位，转换 `×1×1000`
  - `|raw| ≥ 20000` → µA 单位（规范），不乘
  最终统一 `/1000` 得到 mA。
- **容量单位转换** (`BatteryUtils.readCapacityMAh`)：`BATTERY_PROPERTY_CHARGE_COUNTER` 返回 µAh，需除以 1000 转为 mAh。
- **电流降级**：`CURRENT_NOW` → `CURRENT_AVERAGE` 自动降级，两者都不可用时返回 0。

## 开发规划

### 第一阶段 ~~v1.1~~ — 已完成

| 任务 | 说明 |
|------|------|
| 修复图标颠倒 | ✅ 充电/放电图标互换 |
| 理清电流计算逻辑 | ✅ CURRENT_AVERAGE 降级 + 移除阈值 hack |
| 删除死代码 | ✅ 移除注释代码、无用 import |
| 提取字符串资源 | ✅ 所有硬编码文本 → strings.xml |
| 电池百分比展示 | ✅ 状态卡片右侧 |
| ProGuard | ✅ minifyEnabled true |
| contentDescription | ✅ 所有图标无障碍描述 |
| 移除无效权限 | ✅ BATTERY_STATS |
| 暗色模式 | ✅ values-night/colors.xml |
| Power 标题 | ✅ 补全被注释掉的标签 |
| 项目结构重构 | ✅ BatteryInfo 模型 + BatteryUtils 工具类 |
| 左侧导航菜单 | ✅ DrawerLayout + NavigationView |

### 第二阶段 v1.2 — 进行中

| 任务 | 优先级 | 状态 |
|------|--------|------|
| 左侧导航菜单 | P0 | ✅ 已完成 |
| 电池健康状态 | P0 | ✅ 已完成 |
| 充电技术检测 | P1 | ✅ 已完成 |
| 实时图表 | P1 | ✅ 已完成（电压/电流/温度，点击卡片切换） |
| 电池历史记录 | P2 | 待开发 |
| 设置页面 | P2 | 待开发 |
| 充放电通知 | P2 | 待开发 |
| 电池估算时间 | P2 | 待开发 |

### 第三阶段 v2.0 — 规划中

| 任务 | 优先级 |
|------|--------|
| MVVM 重构（ViewModel + LiveData） | P0 |
| Kotlin 迁移 | P1 |
| ForegroundService 后台监控 | P1 |
| 桌面 Widget | P2 |
| 单元测试（JUnit + Mockito） | P2 |
| CI/CD（GitHub Actions） | P2 |
| 多语言支持 | P2 |

## 构建 & 运行

```bash
./gradlew assembleDebug       # Debug 构建
./gradlew assembleRelease     # Release 构建
./gradlew installDebug        # 安装到设备
```
