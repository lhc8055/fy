# Seyra 手机打包说明

这个项目已经基于 `AndroidLiquidGlass` 改成了 `Seyra` 的工作区首版界面。

## 已完成内容

- 应用名改为 `Seyra`
- APK 包名改为 `com.seyra.app`
- 启动页改为 Seyra 工作区
- 工作区使用双列液态玻璃圆角卡片
- 底部 dock 参考你的图片 1，包含 `工作 / 资源 / 工具 / 我的`
- 已加入 GitHub Actions 云端打包配置

## 手机打包方式

1. 用手机浏览器打开 GitHub。
2. 新建一个仓库，例如 `Seyra`。
3. 把这个项目上传到仓库。
4. 打开仓库里的 `Actions`。
5. 选择 `Build Android APK`。
6. 点 `Run workflow`。
7. 等构建完成后，进入这次运行记录。
8. 在 `Artifacts` 里下载 `android-debug-apk`。
9. 解压后里面就是 APK。

## 主要代码位置

- Seyra 工作区界面：`app/src/commonMain/kotlin/com/kyant/backdrop/catalog/SeyraWorkspaceContent.kt`
- 启动入口：`app/src/commonMain/kotlin/com/kyant/backdrop/catalog/MainContent.kt`
- 云端打包配置：`.github/workflows/build-android-apk.yml`

## 下一步建议

现在卡片里的功能名还是占位内容。你下一步只要告诉我每个卡片真实要做什么功能，我就可以继续把工作区功能做出来。
