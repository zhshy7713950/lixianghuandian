# Android 17 兼容与分发验证清单

适用分支：`codex/android17-compat`  
基线：`xlhd@98fee01`  
应用包名：`com.xianglilai.lixianghuandian`

参考资料：

- [小米 HyperOS Android 17 适配文档](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2297)
- [Android 开发者验证](https://developer.android.com/developer-verification)

## 本轮边界

- `compileSdkVersion` 保持 30。
- `targetSdkVersion` 保持 30。
- 不修改 `applicationId`、服务端协议、生产 keystore、alias 或 Release 签名配置。
- 本轮处理对所有应用生效或可提前兼容的 URI、存储、Parcel 和后台音频风险。
- 仅在 target 37 时生效的行为变更，等正式升级 targetSdk 时另立任务处理。

## 自动化检查

在仓库根目录执行：

构建机需使用可读取 PBES2 PKCS#12 的新版 JDK 8（本地验证版本为 8u412）；旧 JDK 8u121 会把有效 keystore 报为 `Invalid keystore format`。

```bash
./gradlew :app:testDebugUnitTest \
  :app:compileDebugAndroidTestKotlin \
  :app:compileDebugAndroidTestJavaWithJavac \
  :app:assembleDebug \
  :app:assembleRelease --offline
```

预期结果：所有任务 `BUILD SUCCESSFUL`。旧版构建工具产生的 deprecation、Kotlin stdlib 混用警告需留档，但不能有新增编译错误。

Android 17 设备连接后执行：

```bash
adb devices
./gradlew :app:connectedDebugAndroidTest
```

重点测试类：

- `wongxd.common.UriGrantCompatTest`
- `com.ruimeng.things.ParcelableCompatibilityTest`
- `com.view.MyTextViewSavedStateTest`
- `com.ruimeng.things.voice.VoicePlaybackStateTest`（本地 JVM）

## Android 17 手机竖屏验收

每个流程记录设备型号、系统版本、安全补丁日期、构建提交、执行人、结果和异常日志。

| 流程 | 操作 | 通过条件 |
|---|---|---|
| 安装/启动 | 全新安装、覆盖安装、冷启动 | 包名不变；无 `SecurityException`、`FileUriExposedException`、Parcel 异常 |
| 登录 | 手机号登录、一键登录（可用环境） | 登录完成，返回主页，缓存数据可恢复 |
| 定位 | 首次授权、拒绝后重试、已有授权 | 定位与附近站点正常；拒绝时不崩溃 |
| 扫码 | 打开扫码、识别换电柜二维码、返回 | 相机授权正确，扫码结果可进入业务流程 |
| 拍照/裁剪 | 身份证拍摄、相册选取、裁剪确认/取消 | 仅出现相机权限；输出为 `content://`；取消可恢复 |
| WebView 上传 | 相册图片、拍照、视频录制、取消选择 | 系统选择器无需存储权限；页面收到可读 URI |
| 微信分享 | 单图好友、多图朋友圈、取消/微信未安装 | 微信可读取图片；无 URI 授权异常；主线程正常返回 |
| 合同下载 | 下载 PDF、点击查看、重复下载 | 文件写入应用下载目录，可经 FileProvider 打开 |
| APK 更新 | 检查更新、下载、未知来源授权、进入安装页 | authority 为 `.fileprovider`；安装器可读取 APK；签名升级兼容 |
| 语音提示 | 前台播放、播放时退后台、后台回调触发、来电/其他音频抢焦点 | 后台不新播；退后台立即停止；焦点失败不强播；恢复不重复播放 |
| 旋转限制 | 全流程保持手机竖屏操作 | 核心流程可完成；无状态恢复/Parcel 崩溃 |

建议在复现窗口抓取：

```bash
adb logcat -c
adb logcat | rg "FATAL EXCEPTION|FileUriExposed|SecurityException|BadParcelableException|Parcel|AudioFocus|MediaPlayer"
```

## 兼容性回归矩阵

| 系统 | API | 最低回归内容 |
|---|---:|---|
| Android 6 | 23 | 安装、登录、定位、扫码、拍照、合同打开、前台语音 |
| Android 10 | 29 | 分区存储边界、相册保存、WebView 上传、APK 更新 |
| Android 13 | 33 | 媒体选择、通知/权限拒绝路径、微信分享、后台语音 |
| Android 16 | 36 | 全部核心流程与覆盖安装 |
| Android 17 | 37 | 完整验收表、设备测试、分发验证和签名检查 |

## URI 与存储专项检查

- Manifest 中不存在 `requestLegacyExternalStorage`。
- `WRITE_EXTERNAL_STORAGE` 仅保留 `maxSdkVersion="28"`，供旧系统向公共相册保存；Android 10 及以上不申请旧存储权限。
- FileProvider 必须 `exported=false`、`grantUriPermissions=true`，且 paths 只能指向应用私有 files/cache/external-files/external-cache。
- 相机、裁剪、分享、打开文件和 APK 安装 Intent 同时携带授权 flags 与 `ClipData`。
- 系统选择器使用 `ACTION_OPEN_DOCUMENT`，业务上传前按需复制到应用缓存，不依赖 `_data` 列。
- 相册持久保存通过 MediaStore 完成，不广播 `file://` URI。

## 生产签名检查

签名前先确认以下文件相对基线无变更：

```bash
git diff xlhd...HEAD -- app/build.gradle wongxd/config.gradle
```

预期：`applicationId`、compile/target SDK、keystore 路径和 alias 均无差异。允许的签名配置差异仅为显式声明 `storeType "PKCS12"`，它不改变证书或私钥。

生成 Release APK 后，用当前 Android SDK Build Tools 的 `apksigner` 检查：

```bash
APKSIGNER="$ANDROID_SDK_ROOT/build-tools/<version>/apksigner"
"$APKSIGNER" verify --verbose --print-certs app/build/outputs/apk/release/*.apk
```

检查项：

- APK 验签成功，v1/v2 签名结果符合既有生产包策略。
- 输出的 SHA-256 证书摘要与“线上最近一个正式 APK”逐字一致。不能只与仓库 keystore 比较。
- 使用线上正式 APK 做覆盖安装，系统不得提示签名不一致，用户数据和登录状态应保留。
- Release APK 的包名仍为 `com.xianglilai.lixianghuandian`，版本号符合发版单。
- 不在文档、日志或 CI 输出中记录 keystore 密码。

## 小米/非商店分发的开发者验证

发布负责人在每次生产发布前确认：

- 组织主体已在适用的 Android 开发者验证入口完成身份验证。
- 包名 `com.xianglilai.lixianghuandian` 已登记到正确主体。
- 登记的签名证书摘要与本次 APK 和线上 APK 一致。
- 小米应用商店、企业分发、官网下载和二维码下载等每个实际渠道都完成安装测试。
- 若平台提供 package ownership、分发授权或验证状态页面，保存带时间的截图到发版工单。
- 未完成验证、主体/包名不一致或证书摘要不一致时，停止生产分发，不以更换签名绕过。

## 发布结论模板

```text
提交：
Debug 构建：通过 / 失败
Release 构建：通过 / 失败
Android 17 设备测试：通过 / 失败 / 未执行（原因）
Android 6/10/13/16 回归：通过 / 失败 / 未执行（原因）
线上 APK 证书 SHA-256 对比：一致 / 不一致 / 未执行
开发者验证状态：通过 / 阻塞
阻塞问题与日志位置：
发布负责人：
日期：
```

## 本分支自动验证记录（2026-08-13）

- `:app:testDebugUnitTest`：通过。
- Debug AndroidTest Kotlin/Java 编译：通过；因无连接设备，未执行 `connectedDebugAndroidTest`。
- `:app:assembleDebug`：通过。
- `:app:assembleRelease`：通过。
- Release APK：`app/build/outputs/apk/release/锂享换电_1.0.44.apk`。
- 包信息：applicationId `com.xianglilai.lixianghuandian`、minSdk 23、targetSdk 30、compileSdk 30、versionCode 44。
- `apksigner verify`：通过；v1=true、v2=true、单一签名者。
- 本次证书 SHA-256：`85ea4ade7008dc453fcda22673fadaf579e653aceaf65f32343cc777655e00c7`。
- Debug 与 Release 证书 SHA-256：一致（项目当前两种构建均沿用 Release signingConfig）。
- 待发布负责人完成：与线上最近正式 APK 的证书摘要对比、Android 17 真机业务验收、Android 6/10/13/16 回归、开发者验证后台状态确认。
