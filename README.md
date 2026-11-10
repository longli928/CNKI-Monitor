# CNKI Monitor

CNKI 相关 Monitor。

## 构建

GitHub Actions 自动编译 release 包，见 [Releases](https://github.com/longli928/CNKI-Monitor/releases)。本地构建：

```bash
gradle --no-daemon :app:assembleRelease
```

输出：`app/build/outputs/apk/release/app-release.apk`

## 环境

- JDK 17、AGP 8.7.3、Gradle 8.9
- compileSdk 34 / minSdk 29
- Xposed API 82（compileOnly，运行时由 LSPosed 提供）
- Kotlin 2.0.21
