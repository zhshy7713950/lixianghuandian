# 理想换电 Android 应用

## 项目概述
这是一个基于 Android 的换电应用，提供智能客服、设备管理等功能。

## 最近更新

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
