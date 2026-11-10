# CNKI LSPosed Starter

这是一个可由 GitHub Actions 自动编译的 Kotlin + LSPosed/Xposed 模块工程骨架。

## 当前实现范围

- 仅在 `com.cnki.android.cnkimobile` 进程加载时输出一条模块加载提示。
- 不读取目标应用私有目录。
- 不读取、复制、导出或恢复 `SharedPreferences`、XML、Token、Cookie 或其他认证会话材料。
- 不执行 `su`，不修改目标应用数据。
- 不包含自动续期或网络登录逻辑。

因此，本仓库是用于验证 **LSPosed 模块工程结构、入口加载和 CI 构建链路** 的安全起点，不是账号备份/切换成品。

## 环境

- JDK 17
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- compileSdk 35 / minSdk 26
- Xposed API compileOnly 82（运行时由 LSPosed 提供）
- Kotlin 2.0.21

说明：LSPosed 的“API 93”是框架侧 API level，不应简单理解为 Maven 依赖版本号。此工程使用兼容的 Xposed API 编译接口；真机加载情况仍需在目标设备验证。

## GitHub 自动编译

1. 新建 GitHub 仓库。
2. 上传本项目全部文件。
3. 推送到 `main` 或 `master` 分支。
4. 打开仓库的 **Actions** 页面。
5. 等待 `Android CI` 完成，在构建任务的 Artifacts 下载 `cnki-lsposed-starter-debug`。

也可以在 Actions 页面手动运行 `Android CI`。

## 本地构建

```bash
gradle --no-daemon :app:assembleDebug
```

输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## LSPosed

安装 APK 后，在 LSPosed 中启用模块，并将作用域限定为：

```text
com.cnki.android.cnkimobile
```

重启目标应用进程后，可在 LSPosed 日志中查看：

```text
[CNKI-LSPOSED] Target process loaded: com.cnki.android.cnkimobile
```

## 尚未验证

- 未在真实 Android 10 / Magisk / LSPosed 1.8.6 设备上安装测试。
- 未验证知网 `PreService` 类是否可见或可 Hook。
- GitHub Actions 尚未实际运行，因此不能声称 APK 已成功编译。
