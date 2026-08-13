# 理想换电 Android 应用

## 项目概述
这是一个基于 Android 的换电应用，提供智能客服、设备管理等功能。

## 最近更新

### 定位协程重复恢复崩溃修复（2026年）

#### 问题说明
系统 GPS 与网络定位可能几乎同时返回结果。旧实现会用两个结果重复恢复同一个协程，导致 `IllegalStateException: Already resumed` 并使 APP 崩溃；权限缺失或定位服务关闭时还可能让协程永久等待。

#### 修复方案
- `LocationUtil.resolveLocation(context, timeoutMillis)` 使用可取消协程获取一次定位，GPS 与网络定位只接受最先到达的结果。
- 页面或 ViewModel 销毁、定位成功、失败及 10 秒超时时都会注销系统监听，避免重复回调和监听泄漏。
- 实时定位失败时优先使用本进程上一次成功坐标，没有缓存时返回 `0,0`；结果中的 `LocationSource` 和 `LocationFailure` 可用于区分来源与失败原因。
- 首页广告信息和两个 Banner 共用一次定位；广告、优惠券和网点列表可以使用降级坐标继续加载，城市反查不会使用 `0,0`。
- 网点页在定位权限拒绝、服务关闭或超时时仍会关闭“定位中”弹窗并展示列表。

#### 返回结果
```kotlin
val result = LocationUtil.resolveLocation(context)
result.coordinates // latitude、longitude
result.source      // LIVE、MEMORY_CACHE、DEFAULT_ZERO
result.failure     // 成功时为 null，失败降级时说明原因
```

### VIVO 审核中隐藏三方广告 Banner（2026年）

#### 功能描述
应对 VIVO 应用商店审核对三方广告「诱导付费」的拦截：当本地 APP 版本高于后管平台版本时，判定为「审核中」，全局隐藏三方广告 Banner；已上线则正常显示。

#### 判断规则
| APP 版本 | 平台版本 | 状态 | 广告 |
| --- | --- | --- | --- |
| 1.0.43 | 1.0.42 | 审核中 | 隐藏 |
| 1.0.43 | 1.0.43 | 已上线 | 显示 |
| 1.0.43 | 1.0.44 | 已上线 | 显示 |

#### 接口
- 路径：`/apiv6/message/getnewappver`
- 参数：`package_name`、`os`、`appType`（不传 ver）
- 平台版本字段：`updateVer`（如 `"1.0.42"`）

#### 实现要点
- APP 启动时在 `HomeViewModel.checkAdStatusSilently()` 拉取一次平台版本并缓存到 `AdManager`
- 各广告位加载前统一走 `AdManager.isAdEnabled()`（审核中强制 false）
- 审核中不初始化 AdScope SDK

#### 相关文件
- `AdManager.kt`：审核状态门禁
- `HomeViewModel.kt`：启动时拉取并比较版本
- `AMPSNativeAdLoader.kt`：关闭时隐藏容器
- `Api.kt` / `BizService.kt` / `GetNewAppVerLocal/Remote`

### 首页无套餐增加「查看电池型号与尺寸」（2026年）

#### 功能描述
在未实名、无押金、无租金场景下，增加「查看电池型号与尺寸」入口，点击后打开 H5 查看对应城市电池规格。

#### 展示规则
| 场景 | 是否展示 |
| --- | --- |
| 未实名 / 无押金 / 无租金 | 是 |
| 已逾期 | 否 |

#### UI
- 黄色问号图标 + 文案「查看电池型号与尺寸」（字色 `#f4de67`）
- 上间距 20dp（样式参考「逾期费用说明」）

#### 跳转
- 打开方式同帮助中心二级网页：`HelpCenterWebFragment`
- 链接：`https://xllbackup.scxll.cn/appH5/SC-DCDGGHCC.html?city={城市名}`
- `city` 取当前用户城市名；未传时 H5 默认展示上海市尺寸

#### 相关文件
- `home_status_no_item.xml`：新增 `tvBatterySpecInfo`
- `FgtHome.kt`：场景显隐与跳转逻辑
- `ic_question_yellow.png`：黄色问号图标

### 首页套餐冻结后展示冻结时间与剩余天数（2026年）

#### 功能描述
套餐冻结后，首页补充展示「冻结时间」「剩余天数」，避免用户看不到剩余天数而反复咨询客服。

#### 展示规则
| 状态 | 租电套餐 | 换电次数 |
| --- | --- | --- |
| 生效中 | 有效期至 + `exp_time`（不变） | 有效期至 + `exp_time`（不变） |
| 已冻结 | 冻结时间 + `stop_time`（系统黄色） | 剩余天数 + `surplus_days`（系统黄色） |

#### 相关字段
- `stop_time`：冻结时间（仅冻结时返回）
- `surplus_days`：剩余天数（仅冻结时返回）

#### 相关文件
- `PaymentDetailBean.kt`：新增 `stop_time`、`surplus_days`
- `FgtHome.kt`：`showPackageInfo()` 按冻结状态切换文案与字色

### 首页无套餐占位图替换为「新人指南」（2026年）

#### 功能描述
针对用户进入 APP 后不清楚操作流程的问题，将首页无套餐场景的占位图替换为「新人指南」步骤图（网络图），套餐逾期场景仍使用「没有电池」图。

#### 场景说明
| 场景 | 占位图 | 尺寸 |
| --- | --- | --- |
| 未实名 / 未交押金 / 未交租金 | 新人指南 | 宽=屏宽 80%，高度等比适配 |
| 套餐逾期 | 没有电池 | 宽 270dp，高 170dp |

#### UI 调整
- 图片与下方文字间距：40dp

#### 图片地址
- 新人指南：`https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/首页-新人指南.png`
- 没有电池：`https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/首页-没有电池.png`

#### 相关文件
- `home_status_no_item.xml`：占位图布局与间距
- `FgtHome.kt`：按场景加载网络图并设置尺寸
- 已删除本地 `home_bg.png` 资源

### 免押权益支持城市扩展（2026年）

#### 功能描述
支付押金页面中，芝麻信用免押取消后可引导购买「免押权益」的城市范围扩展。

#### 支持城市
- 上海市
- 成都市
- 资阳市（新增）

#### 相关文件
- `FgtDeposit.kt`：城市判断逻辑（芝麻信用取消后展示免押权益选项）

### 语音播放管理器功能实现 (2024年)

#### 功能描述
实现了全局语音播放管理器单例，支持网络URL语音播放和语音开关控制。无论哪个页面的播放都通过单例管理，支持播放中断和语音开关状态检查。

#### 技术实现
1. **单例模式**: 使用线程安全的单例模式确保全局唯一实例
2. **网络播放**: 支持播放网络URL语音文件
3. **播放控制**: 支持播放、暂停、停止、中断等功能
4. **音频焦点**: 自动管理音频焦点，支持与其他音频应用协调
5. **语音开关**: 每次播放前检查本地语音开关状态
6. **错误处理**: 完善的错误处理和回调机制

#### 核心组件
- **VoicePlayerManager**: 语音播放管理器单例类
- **VoicePlayerExample**: 使用示例和最佳实践
- **音频焦点管理**: 支持Android 8.0+的AudioFocusRequest
- **网络播放**: 基于MediaPlayer的网络音频播放

#### 功能特点
- **全局管理**: 单例模式，任何页面都可以调用
- **播放中断**: 新播放请求会自动中断当前播放
- **语音开关**: 自动检查isVoiceActived状态，关闭时不播放
- **音频焦点**: 智能管理音频焦点，避免与其他应用冲突
- **错误处理**: 完善的错误处理和用户反馈
- **资源管理**: 自动释放MediaPlayer资源，避免内存泄漏

#### 使用方法
1. **基本播放**: 
   ```kotlin
   VoicePlayerManager.getInstance().playVoice(context, "fail-1")
   ```

2. **带回调播放**:
   ```kotlin
   VoicePlayerManager.getInstance().playVoice(
       context = context,
       voiceId = "fail-1",
       onPlayComplete = { /* 播放完成 */ },
       onPlayError = { errorMsg -> /* 错误处理 */ }
   )
   ```

3. **停止播放**:
   ```kotlin
   VoicePlayerManager.getInstance().stopCurrentPlay()
   ```

4. **检查状态**:
   ```kotlin
   val isPlaying = VoicePlayerManager.getInstance().isPlaying()
   ```

#### 语音URL规则
- **基础URL**: `https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/voice/`
- **文件扩展名**: `.mp3`
- **完整URL**: `基础URL + voiceId + 扩展名`
- **示例**: `fail-1` → `https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/voice/fail-1.mp3`

#### 文件结构
```
app/src/main/java/com/ruimeng/things/voice/
├── VoicePlayerManager.kt          # 语音播放管理器单例
└── VoicePlayerExample.kt          # 使用示例和最佳实践
```

#### 技术特点
- **线程安全**: 使用@Volatile和synchronized确保线程安全
- **资源管理**: 自动释放MediaPlayer和音频焦点资源
- **兼容性**: 支持Android 8.0+的AudioFocusRequest和旧版本兼容
- **错误处理**: 完善的异常处理和用户反馈机制
- **性能优化**: 单例模式避免重复创建，资源复用
- **用户体验**: 智能音频焦点管理，避免音频冲突

#### 注意事项
1. 播放前会自动检查语音开关状态，关闭时不会播放
2. 新的播放请求会自动中断当前播放
3. 建议在页面销毁时调用stopCurrentPlay()释放资源
4. 支持网络URL播放，需要网络权限
5. 音频焦点管理确保与其他音频应用协调工作

### 语音提示开关功能实现 (2024年)

#### 功能描述
在设置页面新增语音提示开关功能，用户可以手动控制语音提示的开启和关闭状态。支持实时状态同步和本地状态持久化。

#### 技术实现
1. **接口集成**: 新增`/apiv6/user/voiceopen`接口用于更新语音开关状态
2. **数据模型**: 在`UserInfoBean`中新增`isVoiceActived`字段存储语音开关状态
3. **UI组件**: 在设置页面添加Switch开关组件，支持状态切换
4. **状态管理**: 使用`UserInfoLiveData`管理用户信息状态，确保数据一致性

#### 核心组件
- **FgtSetting**: 设置页面，集成语音开关UI和交互逻辑
- **VoiceOpenLocal/Remote**: 语音开关接口的请求和响应实体类
- **BizService**: 网络服务层，封装语音开关接口调用
- **UserInfoBean**: 用户信息数据模型，新增语音开关状态字段

#### 功能特点
- **实时状态**: 开关状态实时同步到服务器
- **状态持久化**: 本地保存用户设置，页面刷新后状态保持
- **错误处理**: 接口调用失败时自动恢复开关状态
- **用户反馈**: 操作成功/失败时显示相应的Toast提示
- **状态显示**: 开关旁边显示当前状态文本（已开启/已关闭）

#### 使用方法
1. **进入设置页面**: 在"我的"页面点击"设置"进入设置页面
2. **操作语音开关**: 点击"语音提示"右侧的Switch开关
3. **状态确认**: 开关状态会实时更新，并显示相应的状态文本
4. **状态同步**: 操作成功后状态会同步到服务器，返回"我的"页面时会自动刷新

#### 文件结构
```
app/src/main/
├── java/com/ruimeng/things/
│   ├── bean/
│   │   └── UserInfoBean.kt                    # 用户信息数据模型（新增isVoiceActived字段）
│   └── me/
│       └── FgtSetting.kt                      # 设置页面（集成语音开关功能）
├── java/com/entity/
│   ├── local/
│   │   └── VoiceOpenLocal.kt                  # 语音开关请求实体
│   └── remote/
│       └── VoiceOpenRemote.kt                 # 语音开关响应实体
├── java/com/net/call/
│   ├── Api.kt                                 # 接口定义（新增Voice_Open）
│   └── BizService.kt                          # 网络服务（新增voiceOpen方法）
└── res/
    └── layout/
        └── fgt_setting.xml                    # 设置页面布局（新增语音开关UI）
```

#### 接口说明
- **接口地址**: `/apiv6/user/voiceopen`
- **请求方式**: POST
- **入参**: `{ "userId": "用户ID", "isVoiceActived": 1 }` (1-启用，0-关闭)
- **返回值**: `{ "errcode": 0, "errmsg": "操作成功" }`

#### 技术特点
- **SOLID原则**: 遵循单一职责原则，每个类只负责特定功能
- **异常处理**: 完善的异常处理机制，确保应用稳定性
- **状态管理**: 使用LiveData管理状态，支持响应式更新
- **用户体验**: 操作反馈及时，状态显示清晰
- **代码质量**: 添加详细注释，遵循命名规范
- **架构清晰**: 遵循MVVM架构，代码结构清晰

#### 注意事项
1. 语音开关状态会同步到服务器，确保多设备间状态一致
2. 接口调用失败时会自动恢复开关状态，避免状态不一致
3. 建议在网络良好的环境下操作，确保状态同步成功
4. 语音开关状态会影响APP内的语音提示功能

### 通用警告提醒弹窗功能实现 (2024年)

#### 功能描述
实现了通用警告提醒弹窗功能，可以用于各种需要显示警告信息的场景，如电池高温、设备故障、网络异常等。支持自定义原因说明和处理办法文本。

#### 技术实现
1. **弹窗组件**: 创建`WarningAlertPopupWindow`类，继承自`PopupWindow`
2. **UI设计**: 采用ConstraintLayout布局，实现精确的UI定位
3. **渐变背景**: 使用layer-list实现从上到下的渐变背景效果
4. **图标复用**: 复用退租弹窗中的感叹号图标`@mipmap/ic_alert`
5. **参数化设计**: 支持自定义原因说明和处理办法文本
6. **通用性设计**: 类名和文件名采用通用命名，适用于各种警告场景

#### 核心组件
- **WarningAlertPopupWindow**: 通用警告提醒弹窗主类
- **WarningAlertExample**: 使用示例类，展示多种场景下的调用方法
- **popup_warning_alert.xml**: 弹窗布局文件
- **bg_warning_alert_popup.xml**: 弹窗背景样式文件

#### 使用方法
1. **电池高温提醒**: 显示电池高温警告信息
```kotlin
val popup = WarningAlertPopupWindow(
    fgtBase = this,
    reason = "由于电池或者保护板高温，您的电池即将或者已经断电",
    solution = "请您立即停止行驶，并将电池取出，静置3~5分钟，等待温度恢复正常后，即可重新放电继续使用"
)
popup.show(anchorView)
```

2. **设备故障提醒**: 显示设备异常警告信息
```kotlin
val popup = WarningAlertPopupWindow(
    fgtBase = this,
    reason = "检测到设备异常，可能存在安全隐患",
    solution = "请立即停止使用设备，联系客服进行检修，确保安全后再继续使用"
)
popup.show(anchorView)
```

3. **自定义内容**: 传入自定义的原因说明和处理办法
```kotlin
val popup = WarningAlertPopupWindow(
    fgtBase = this,
    reason = customReason,
    solution = customSolution
)
popup.show(anchorView)
```

#### UI特点
- **背景**: 30%透明度的黑色背景遮罩
- **主容器**: 左距右距60px，上距180px，圆角20px
- **感叹号图标**: 60px x 60px，上距150px，复用退租弹窗图标
- **标题**: "温馨提示"，40px字号，加粗，上距40px
- **原因说明**: 16px字号，居中对齐，左距右距20px，上距30px
- **处理办法**: 16px字号，红色字体，居中对齐，左距右距20px，上距10px
- **按钮**: "好的"按钮，120px x 34px，圆角17px，红色背景，白色字体
- **渐变背景**: 高度110px，从#ffe556到#FFFFFF的渐变

#### 文件结构
```
app/src/main/
├── java/com/ruimeng/things/home/view/
│   ├── WarningAlertPopupWindow.kt        # 通用警告提醒弹窗主类
│   └── WarningAlertExample.kt            # 使用示例类
├── res/
│   ├── layout/
│   │   └── popup_warning_alert.xml       # 弹窗布局文件
│   └── drawable/
│       └── bg_warning_alert_popup.xml    # 弹窗背景样式文件
```

#### 技术特点
- **通用性设计**: 采用通用命名，适用于各种警告提醒场景
- **SOLID原则**: 遵循单一职责原则，弹窗类只负责显示和交互
- **参数化设计**: 支持自定义文本内容，提高复用性
- **UI精确控制**: 使用dp单位确保UI在不同设备上的一致性
- **代码质量**: 添加详细注释，遵循命名规范
- **异常处理**: 包含必要的空值检查和错误处理
- **用户体验**: 点击按钮或外部区域可关闭弹窗
- **场景丰富**: 提供多种使用场景示例，便于快速集成

#### 注意事项
1. 弹窗需要在Activity的windowToken有效时才能显示
2. 建议在Fragment的onViewCreated中调用弹窗显示方法
3. 文本内容支持中英文，建议根据实际需求调整字体大小
4. 弹窗会自动处理生命周期，无需手动管理

### 全局广告开关功能实现 (2024年)

#### 功能描述
实现了全局广告开关控制功能，通过后端接口动态控制APP内所有广告的显示状态，确保广告内容的安全性和可控性。

#### 技术实现
1. **接口集成**: 在启动页调用`apiv6/advertisementinfo/getthridadstatus`接口获取广告开关状态
2. **单例管理**: 创建`AdManager`单例类统一管理广告开关状态
3. **ViewModel架构**: 使用`SplashViewModel`处理启动页的广告状态检查逻辑
4. **状态持久化**: 广告开关状态在APP运行期间保持，支持实时控制

#### 核心组件
- **AdManager**: 广告管理单例类，提供广告开关状态管理和SDK自动初始化
- **SplashViewModel**: 启动页ViewModel，处理广告状态检查
- **GetThirdAdStatusLocal/Remote**: 广告开关接口的请求和响应实体类
- **BizService**: 网络服务层，封装广告开关接口调用

#### 使用方法
1. **检查广告状态**: 使用`AdManager.getInstance().isAdEnabled()`检查广告是否允许显示
2. **检查SDK状态**: 使用`AdManager.getInstance().isSdkInitialized()`检查SDK是否已初始化
3. **获取状态描述**: 使用`AdManager.getInstance().getStatusDescription()`获取状态描述
4. **状态控制**: 根据`AdManager`的状态控制广告相关UI的显示和隐藏
5. **手动初始化**: 使用`AdManager.getInstance().manualInitSdk()`手动触发SDK初始化

#### 文件结构
```
app/src/main/java/com/ruimeng/things/
├── ads/
│   ├── AdManager.kt                    # 广告管理单例类
│   └── AdManagerUsageExample.kt        # 使用示例
├── SplashViewModel.kt                  # 启动页ViewModel
└── AtySplash.kt                        # 启动页Activity（已集成广告开关检查）

app/src/main/java/com/entity/
├── local/GetThirdAdStatusLocal.kt      # 广告开关请求实体
└── remote/GetThirdAdStatusRemote.kt    # 广告开关响应实体

app/src/main/java/com/net/call/
├── Api.kt                              # 添加广告开关接口定义
└── BizService.kt                       # 添加广告开关接口调用方法
```

#### 接口说明
- **接口地址**: `apiv6/advertisementinfo/getthridadstatus`
- **请求方式**: POST
- **入参**: 无
- **返回值**: `{ "switch": 1 }` (1-开，2-关)

#### 技术特点
- **实时控制**: 每次APP启动时检查广告开关状态
- **自动初始化**: 广告开关打开时自动初始化AdScope SDK
- **安全优先**: 接口失败时默认关闭广告，确保安全性
- **状态管理**: 使用单例模式确保全局状态一致性
- **架构清晰**: 遵循MVVM架构，代码结构清晰
- **异常处理**: 完善的异常处理机制，确保应用稳定性
- **SDK状态跟踪**: 实时跟踪SDK初始化状态，支持状态查询

#### 注意事项
1. 广告开关状态仅在APP启动时获取，运行期间不会自动更新
2. 接口失败时默认关闭广告，确保不会显示不合适的广告内容
3. 建议在显示广告前都调用`AdManager.getInstance().isAdEnabled()`和`isSdkInitialized()`进行最终检查
4. 需要将`AdManager`中的`AMPS_APPID`替换为从AdScope开发者后台获取的实际AppId
5. SDK初始化是异步的，建议在显示广告前检查`isSdkInitialized()`状态

### AdScope聚合广告SDK接入 (2024年)

#### 功能描述
成功接入AdScope聚合广告SDK，支持多个广告渠道的聚合展示，包括：
- **倍孜广告渠道**: 提供开屏、原生、激励视频、插屏、横幅等多种广告形式
- **广点通（优量汇）**: 腾讯广告平台，支持多种广告类型
- **快手广告渠道**: 快手广告平台，提供丰富的广告资源
- **穿山甲/GroMore**: 字节跳动广告平台，支持多种广告形式

#### 技术实现
1. **依赖配置**: 在`app/build.gradle`中添加了AdScope核心SDK和各个渠道的适配器依赖
2. **Maven仓库**: 在根目录`build.gradle`中配置了所需的Maven仓库地址
3. **混淆配置**: 在`proguard-rules.pro`中添加了完整的混淆规则，确保SDK正常工作
4. **文件管理**: 在`app/libs/`目录下创建了SDK文件说明文档

#### 接入的广告渠道
- **倍孜 (BZ)**: 支持开屏、原生、激励视频、插屏、横幅广告
- **广点通 (GDT)**: 支持开屏、原生、激励视频、插屏、横幅广告  
- **快手 (KS)**: 支持开屏、原生、激励视频、插屏、横幅广告
- **穿山甲 (CSJ)**: 支持开屏、原生、激励视频、插屏、横幅广告
- **GroMore (GM)**: 支持开屏、原生、激励视频、插屏、横幅广告

#### 使用方法
1. **SDK文件准备**: 从AdScope开发者后台下载所需的SDK文件，放置到`app/libs/`目录
2. **项目同步**: 同步Gradle项目，确保所有依赖正确加载
3. **初始化SDK**: 在Application中初始化AdScope SDK
4. **广告展示**: 根据业务需求调用相应的广告展示API

#### 文件结构
```
app/
├── build.gradle                    # 添加了AdScope SDK依赖配置
├── proguard-rules.pro             # 添加了AdScope混淆规则
└── libs/
    ├── README_AdScope_SDK.md      # SDK文件说明文档
    └── [AdScope SDK文件]          # 需要从后台下载的SDK文件
```

#### 注意事项
1. 需要从AdScope开发者后台下载对应的SDK文件
2. 确保SDK版本与配置中的版本号匹配
3. 在正式使用前需要完成SDK初始化配置
4. 建议在测试环境充分测试后再发布到生产环境

### 变更手机号码页面验证码输入框优化 (2024年)

#### 问题描述
在变更手机号码功能中，验证码输入框的提示文字和间距需要优化：
1. **验证码输入框placeholder**：需要从"请输入验证码"改为"请输入短信验证码"，提供更明确的提示
2. **验证码输入框上边距**：需要根据页面类型设置不同的上边距
   - 验证老手机号码页面：上距25dp
   - 变更新手机号码页面：上距30dp

#### 解决方案
优化变更手机号码页面的用户体验，提供更清晰的提示和合适的间距

##### 主要修改内容

1. **验证码输入框placeholder优化**
   - 将placeholder从"请输入验证码"改为"请输入短信验证码"
   - 提供更明确的用户提示，让用户知道这是短信验证码

2. **验证码输入框间距动态调整**
   - 验证老手机号码页面：验证码输入框上距设置为25dp
   - 变更新手机号码页面：验证码输入框上距设置为30dp
   - 通过代码动态设置，确保不同页面有不同的间距

##### 技术特点
- **用户体验优化**: 提供更清晰的输入提示
- **动态布局**: 根据页面类型动态调整间距
- **代码质量**: 遵循SOLID原则，保持代码简洁
- **兼容性**: 同时适用于验证老手机号码和变更新手机号码两个页面
- **精确控制**: 使用dp单位确保在不同屏幕密度下的一致性

#### 使用方法

1. **验证老手机号码**: 输入框提示"请输入短信验证码"，上距25dp
2. **变更新手机号码**: 输入框提示"请输入短信验证码"，上距30dp

#### 文件结构
```
app/src/main/res/layout/
└── fgt_change_mobile.xml                    # 变更手机号码页面布局（已优化）

app/src/main/java/com/ruimeng/things/home/
└── FgtChangeMobile.kt                       # 变更手机号码页面逻辑（已优化）
```

#### 注意事项
1. 修改同时影响验证老手机号码和变更新手机号码两个页面
2. 间距通过代码动态设置，确保不同页面有不同的视觉效果
3. 建议在不同屏幕尺寸设备上测试布局效果

### 登录页面UI微调优化 (2024年)

#### 问题描述
用户反馈登录页面UI需要微调，包括输入框间距、按钮圆角、键盘类型和提示信息等方面的优化。

#### 解决方案
对登录页面进行全面的UI微调，提升用户体验

##### 主要修改内容

1. **输入框间距优化**
   - 手机号码输入框上距从50dp调整为30dp，缩小间距
   - 短信验证码输入框上距从30dp调整为20dp，缩小间距

2. **键盘类型统一**
   - 图形验证码输入框键盘类型从`text|textNoSuggestions`改为`number`
   - 与手机号码和短信验证码输入框保持一致的数字键盘类型

3. **按钮圆角统一**
   - 获取验证码按钮圆角从24dp调整为8dp
   - 验证码登录按钮圆角从24dp调整为8dp
   - 与"使用「本机号码一键登录」"按钮保持一致的8dp圆角

4. **验证码登录按钮优化**
   - 上距从50dp调整为30dp，缩小间距
   - 使用新的8dp圆角样式

5. **提示信息更新**
   - 将老版本的简单提示拆分为新版本的3个具体提示：
     - "请输入手机号码(11位)"
     - "请输入图形验证码(5位)"  
     - "请输入短信验证码(6位)"

##### 技术特点
- **UI一致性**: 统一按钮圆角和输入框键盘类型
- **用户体验**: 优化间距布局，提升视觉舒适度
- **提示友好**: 提供更具体和准确的错误提示信息
- **代码质量**: 遵循SOLID原则，保持代码整洁
- **维护性**: 创建可复用的drawable资源文件

#### 使用方法

1. **手机号码输入**: 输入11位手机号码，自动触发图形验证码加载
2. **图形验证码**: 输入5位数字验证码，支持数字键盘
3. **短信验证码**: 输入6位数字验证码，支持数字键盘
4. **验证码登录**: 点击按钮进行登录，提供详细的错误提示

#### 文件结构
```
app/src/main/res/
├── layout/
│   └── aty_login.xml                    # 登录页面布局（已优化）
├── drawable/
│   ├── bg_btn_common1_8px.xml          # 8px圆角按钮样式
│   └── bg_btn_login_8dp.xml            # 8dp圆角登录按钮样式
└── java/com/ruimeng/things/
    └── AtyLogin.kt                      # 登录页面逻辑（已优化）
```

#### 注意事项
1. 所有输入框现在都使用数字键盘，提升输入体验
2. 按钮圆角已统一为8dp，保持UI一致性
3. 错误提示更加具体，帮助用户快速定位问题

### 扫描开门页面按钮位置优化 (2024年)

#### 问题描述
在 `fgt_scan_open.xml` 布局文件中，按钮位置存在以下问题：
1. **立即换电按钮**位置设置不准确，使用了`layout_gravity="bottom"`但同时又设置了`layout_marginTop="50dp"`
2. **客服中心悬浮按钮**的`layout_marginBottom="100dp"`可能与立即换电按钮产生重叠
3. 布局结构使用LinearLayout，按钮定位不够精确

#### 解决方案
采用**ConstraintLayout重构布局**，精确定位按钮位置

##### 主要修改内容

1. **布局结构优化**
   - 将LinearLayout改为ConstraintLayout，提供更精确的布局控制
   - 为每个主要组件添加唯一ID，便于约束关系管理
   - 使用现代Android布局标准，替换过时的`layout_marginLeft/Right`为`layout_marginStart/End`

2. **立即换电按钮精确定位**
   - 使用`app:layout_constraintBottom_toBottomOf="parent"`精确定位到页面底部
   - 设置`layout_marginBottom="24dp"`提供合适的底部间距
   - 使用`layout_width="0dp"`配合约束实现响应式宽度

3. **客服中心悬浮按钮优化**
   - 调整`layout_marginBottom="80dp"`避免与换电按钮重叠
   - 使用`app:layout_constraintBottom_toBottomOf="parent"`和`app:layout_constraintEnd_toEndOf="parent"`精确定位
   - 保持50dp x 50dp的标准尺寸

4. **布局层次优化**
   - 移除大量注释掉的代码，提高布局文件可读性
   - 为套餐详情布局添加约束关系
   - 优化电池信息卡片的约束设置

##### 技术特点
- **精确布局**: 使用ConstraintLayout实现像素级精确定位
- **响应式设计**: 按钮宽度自适应屏幕尺寸
- **避免重叠**: 科学计算按钮间距，确保UI元素不重叠
- **现代标准**: 使用最新的Android布局属性
- **代码质量**: 遵循SOLID原则，提高代码可维护性
- **用户体验**: 优化按钮位置，提升操作便利性

#### 使用方法

1. **立即换电**: 按钮位于页面底部中央，点击执行换电操作
2. **客服中心**: 悬浮按钮位于右下角，点击进入客服中心页面
3. **布局适配**: 自动适配不同屏幕尺寸，保持按钮位置一致性

#### 文件结构
```
app/src/main/res/layout/
├── fgt_scan_open.xml                    # 扫描开门页面布局（已优化）
└── package_details_layout.xml            # 套餐详情布局

app/src/main/java/com/ruimeng/things/home/view/
└── CustomerServiceFloatingButton.kt     # 客服中心悬浮按钮组件
```

#### 注意事项
1. 确保在Android 5.0+设备上测试布局效果
2. 按钮位置已针对常见屏幕尺寸优化
3. 建议在不同分辨率设备上验证布局效果

### 智能客服页面文件选择功能修复 (2024年)

#### 问题描述
在 `SmartCustomerServiceFragment` 中，当用户选择图片后，`onActivityResult` 方法不执行，导致文件选择功能无法正常工作。

#### 问题原因分析
1. **Fragment 生命周期问题**: `SmartCustomerServiceFragment` 继承自 `BaseBackFragment`，在 Fragment 中 `onActivityResult` 的回调机制与 Activity 不同
2. **Request Code 不匹配**: `CustomWebView` 和 `DefaultFileChooserStrategy` 中定义的请求码不一致
3. **权限处理机制**: 原有的权限处理方式在 Fragment 中可能无法正常工作

#### 解决方案
采用**方案一：使用项目现有的权限框架和Activity结果处理框架**

##### 主要修改内容

1. **DefaultFileChooserStrategy.kt**
   - 统一请求码为 1001
   - 集成项目现有的权限框架 `wongxd.common.permission.Permission`
   - 使用 `getPermissions()` 方法请求相机和存储权限
   - 集成项目现有的 `SimpleOnActivityResult` 框架处理Activity结果
   - 支持相机和存储权限的动态请求

2. **CustomWebView.kt**
   - 简化权限处理逻辑
   - 移除自定义权限请求回调
   - 移除 `handleFileChooserResult` 方法
   - 保持文件选择功能完整性

3. **SmartCustomerServiceFragment.kt**
   - 移除自定义权限请求处理
   - 移除 `onActivityResult` 方法
   - 使用项目现有框架的权限请求和Activity结果处理机制

##### 技术特点
- **框架集成**: 使用项目现有的 `wongxd.common.permission.Permission` 权限框架
- **结果处理**: 使用项目现有的 `SimpleOnActivityResult` 框架处理Activity结果
- **请求码处理**: 不依赖特定的请求码，使用 `currentRequestType` 来区分不同的文件选择操作
- **兼容性**: 完全兼容fragmentation库的Fragment管理
- **用户体验**: 提供清晰的权限请求流程
- **代码质量**: 遵循 SOLID 原则，使用策略模式
- **维护性**: 复用项目现有代码，减少重复实现
- **调试友好**: 添加详细的日志输出，便于问题排查

#### 使用方法

1. **权限请求**: 当 WebView 需要访问相机或存储时，会自动触发权限请求
2. **文件选择**: 支持图片和视频的选择，包括拍照和从相册选择
3. **权限管理**: 自动处理权限授予和拒绝的情况

#### 文件结构
```
app/src/main/java/com/ruimeng/things/home/
├── SmartCustomerServiceFragment.kt    # 智能客服页面
├── webview/
│   ├── CustomWebView.kt               # 自定义WebView组件
│   ├── DefaultFileChooserStrategy.kt  # 默认文件选择策略
│   ├── FileChooserStrategy.kt         # 文件选择策略接口
│   └── HelpCenterUrlStrategy.kt       # 帮助中心URL策略
```

#### 权限要求
在 `AndroidManifest.xml` 中已声明：
- `android.permission.CAMERA` - 相机权限
- `android.permission.WRITE_EXTERNAL_STORAGE` - 存储权限

#### 注意事项
1. 确保在 Android 6.0+ 设备上测试权限请求功能
2. 文件选择功能需要相应的权限支持
3. 建议在真机上测试相机和文件选择功能

## 开发环境
- Android Studio
- Kotlin
- Android SDK 21+
- 支持 Android 5.0 (API 21) 及以上版本

## 构建说明
1. 克隆项目到本地
2. 在 Android Studio 中打开项目
3. 同步 Gradle 依赖
4. 构建并运行项目

## 联系方式
如有问题或建议，请联系开发团队。

## AMPS 原生广告封装使用说明

依赖：已在 `app/build.gradle` 集成 AdScope 相关 AAR。

封装类：`com.ruimeng.things.ads.AMPSNativeAdLoader`

功能：
- 传入外部 `Lifecycle` 自动管理 `resume/destroy`
- 传入容器 `ViewGroup` 自动渲染广告视图
- 提供监听接口，转发加载、展示、点击、关闭、渲染成功/失败事件
- 遵循全局广告总开关与 SDK 初始化状态

示例（在 Fragment 中使用）：
```kotlin
class ExampleFragment : Fragment(R.layout.fragment_example) {
    private var adLoader: AMPSNativeAdLoader? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<ViewGroup>(R.id.adContainer)
        adLoader = AMPSNativeAdLoader(requireActivity(), viewLifecycleOwner.lifecycle)
        adLoader?.loadInto(
            container = container,
            spaceId = "15349", // 替换为后台分配的原生广告位ID
            listener = object : AMPSNativeAdLoader.Listener {
                override fun onLoadSuccess(infoList: List<AMPSNativeAdExpressInfo>) {
                    // 可选：拿到广告实例列表
                }
                override fun onRenderSuccess(view: View, width: Float, height: Float) {
                    // 广告已自动添加到容器
                }
                override fun onLoadFailed(errorCode: Int, message: String?) {
                    // 展示兜底或隐藏容器
                }
            }
        )
    }
}
```

注意：
- 需确保 `AdManager.getInstance().isAdEnabled()` 为 true 且 SDK 已初始化。
- `spaceId` 请替换为 AdScope 后台实际分配的原生广告位 ID。
- 生命周期请传入 Fragment 的 `viewLifecycleOwner.lifecycle` 以避免内存泄漏。

参考文档：[AdScope 原生(NativeExpress) 文档](https://h-doc.adscope.com.cn/docs/85ljgW)

### 电柜详情页广告集成 (2024年)

#### 功能描述
在电柜详情页底部集成原生广告位，支持动态高度、自动布局调整和用户关闭功能。

#### 技术实现
1. **布局集成**: 在 `fgt_net_station_detail_new.xml` 中添加广告容器
2. **代码集成**: 在 `FgtNetStationDetailNew.kt` 中集成 `AMPSNativeAdLoader`
3. **样式设计**: 创建圆角背景样式 `bg_ad_container.xml`
4. **交互逻辑**: 支持广告关闭后自动调整电池列表间距

#### 布局特点
- **位置**: 页面底部，电池列表下方
- **间距**: 左距12px，右距12px，上距10px，下距40px
- **样式**: 圆角10px，白色背景，浅灰色边框
- **高度**: 动态高度，由SDK根据屏幕宽度和广告内容自动计算

#### 交互逻辑
- **广告显示**: 加载成功后自动显示，失败时隐藏容器
- **广告关闭**: 用户点击右上角X按钮后，广告消失，电池列表底部间距恢复为34dp
- **生命周期**: 自动管理广告的resume/destroy，避免内存泄漏

#### 文件结构
```
app/src/main/
├── java/com/ruimeng/things/
│   ├── ads/
│   │   ├── AMPSNativeAdLoader.kt          # 广告加载器封装
│   │   └── AdUsageExample.kt              # 使用示例
│   └── net_station/
│       └── FgtNetStationDetailNew.kt      # 电柜详情页（已集成广告）
├── res/
│   ├── drawable/
│   │   └── bg_ad_container.xml            # 广告容器背景样式
│   └── layout/
│       ├── fgt_net_station_detail_new.xml # 电柜详情页布局（已添加广告容器）
│       └── fragment_ad_example.xml        # 广告使用示例布局
```

#### 使用方法
广告会自动在电柜详情页加载，无需额外操作。如需在其他页面使用，参考 `AdUsageExample.kt` 中的实现方式。

#### 注意事项
1. 确保 `AdManager` 的广告总开关已开启且SDK已初始化
2. 广告位ID `15349` 需要替换为AdScope后台实际分配的ID
3. 广告关闭后会自动调整布局，确保用户体验流畅
