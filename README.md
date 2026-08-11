# SO影视 - 安卓影视应用

一个功能完整的安卓影视观看应用，采用 Kotlin + Jetpack Compose 开发。集成规则系统，支持多源搜索和在线播放。

## 功能特性

### 📋 规则系统（v1.1.0）
- 兼容 Kazumi 规则格式（JSON）
- 支持 XPath 和 API 两种规则模式
- 规则自动安装（首次启动自动加载内置规则）
- 规则仓库：从 GitHub 远程下载规则
- 规则导入/导出（JSON / Base64 分享链接）
- 规则启用/禁用/删除管理
- 多规则并发搜索

### 🔍 在线搜索（增强）
- 多源并发搜索（所有启用规则同时搜索）
- 搜索结果聚合显示
- 显示来源规则标识
- 搜索结果带封面图

### 📺 选集页面（新增）
- 多线路支持（线路切换）
- 剧集列表展示
- 一键跳转播放

### 🏠 首页（推荐）
- 轮播Banner展示热门推荐
- 正在热播推荐列表
- 热门电影推荐列表
- 快捷筛选标签（推荐/电影/电视剧/综艺/动漫）
- 搜索入口

### 📂 分类
- 类型筛选（电影/电视剧/综艺/动漫）
- 剧情分类（剧情/喜剧/动作/爱情/科幻/悬疑等）
- 地区筛选（大陆/香港/台湾/美国/韩国/日本）
- 年份筛选
- 排序方式（最多播放/最新上映/评分最高）
- 网格展示筛选结果

### ❤️ 追剧
- 追更中 / 已完成 / 放弃 三个标签页
- 追剧列表卡片展示
- 快速继续播放
- 追剧进度显示

### 👤 我的
- 用户信息卡片
- VIP会员开通入口
- 快捷功能（观看历史/我的收藏/我的下载/消息中心）
- 更多功能（片单管理/播放设置/清理缓存/帮助与反馈/设置）

### 🎬 播放器（v1.2.0）
- 视频播放控制区（进度条/播放暂停）
- WebView 视频源嗅探
- ExoPlayer 集成播放
- 倍速播放（0.5x ~ 2.0x）
- 全屏切换
- 进度条点击跳转
- 影片基本信息展示
- 快捷操作（收藏/追更/下载/分享）
- 选集列表
- 详情/讨论Tab
- 猜你喜欢推荐

### 💬 弹幕系统（v1.3.0）
- 弹弹play API 集成，自动搜索匹配弹幕
- 自定义 Canvas 高性能弹幕渲染（60fps）
- 支持滚动、顶部、底部三种弹幕类型
- 弹幕时间轴与视频同步
- 弹幕设置面板：
  - 开关弹幕
  - 字体大小调节（10sp ~ 28sp）
  - 透明度调节（20% ~ 100%）
  - 滚动速度调节（0.5x ~ 3.0x）
  - 最大显示数控制（10 ~ 100）
  - 分类开关（滚动/顶部/底部弹幕）
- 智能集数识别（支持中文/数字集名）

### 🎨 元数据增强（新增 v1.4.0）
- Bangumi (bgm.tv) API 集成
- 自动搜索匹配番剧/影视元数据
- 搜索结果增强：
  - Bangumi 封面图回填（规则无封面时自动补充）
  - 评分展示
  - 标签展示
- 选集页面元数据头部：
  - 封面/海报展示
  - 中文/原名显示
  - 评分与评分人数
  - 放送日期与集数
  - 标签列表
  - 剧情简介
- 元数据缓存（内存 + 磁盘，7天过期）
- 智能标题清理（去除集数后缀）

### 🔍 搜索
- 搜索历史记录
- 热门搜索榜（带热/新标签）
- 搜索结果网格展示
- 一键清空历史

### 📋 片单
- 全部/最新/最热/高分排序
- 片单卡片展示

### ⚙️ 播放设置
- 清晰度选择（自动/1080P/720P/480P）
- 播放速度（0.5x ~ 2.0x）
- 跳过片头片尾开关
- 运营商网络自动播放开关
- 允许非Wi-Fi下缓存开关

### ℹ️ 关于
- APP版本信息
- 检查更新
- 用户协议
- 隐私政策
- 联系我们

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose (Material 3)
- **导航**: Navigation Compose
- **图片加载**: Coil
- **视频播放**: Media3 ExoPlayer
- **弹幕系统**: 自定义 Canvas 渲染 + 弹弹play API
- **元数据**: Bangumi (bgm.tv) API
- **HTML解析**: Jsoup (规则XPath解析)
- **JSON解析**: Gson + Jayway JSONPath (API规则)
- **网络请求**: OkHttp
- **架构**: MVVM + ViewModel
- **最低SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)

## 项目结构

```
app/
├── src/main/
│   ├── java/com/so/movie/
│   │   ├── data/
│   │   │   ├── Models.kt          # 数据模型
│   │   │   └── MockData.kt        # 模拟数据
│   │   ├── navigation/
│   │   │   └── Screens.kt         # 导航路由定义
│   │   ├── player/
│   │   │   ├── VideoPlayer.kt       # 播放器(ExoPlayer+弹幕集成)
│   │   │   └── VideoSourceSniffer.kt # WebView视频嗅探
│   │   ├── danmaku/
│   │   │   ├── DanmakuModels.kt     # 弹幕数据模型
│   │   │   ├── DanmakuView.kt       # 自定义弹幕渲染View
│   │   │   └── DanDanPlayApi.kt     # 弹弹play API客户端
│   │   ├── metadata/
│   │   │   ├── MetadataModels.kt    # Bangumi元数据模型
│   │   │   ├── BangumiApi.kt        # Bangumi API客户端
│   │   │   └── MetadataRepository.kt # 元数据缓存仓库
│   │   ├── rule/
│   │   │   ├── RuleModels.kt      # 规则数据模型(兼容Kazumi)
│   │   │   ├── RuleEngine.kt      # 规则引擎(XPath+API)
│   │   │   └── RuleRepository.kt  # 规则仓库(下载/存储/管理)
│   │   ├── ui/
│   │   │   ├── components/        # 通用组件
│   │   │   ├── screen/            # 页面
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── CategoryScreen.kt
│   │   │   │   ├── FollowScreen.kt
│   │   │   │   ├── MineScreen.kt
│   │   │   │   ├── PlayerScreen.kt
│   │   │   │   ├── SearchScreen.kt  # 增强多源搜索
│   │   │   │   ├── ChapterScreen.kt # 选集页面(新增)
│   │   │   │   ├── RuleManagementScreen.kt # 规则管理(新增)
│   │   │   │   └── OtherScreens.kt
│   │   │   └── theme/             # 主题配置
│   │   ├── viewmodel/
│   │   │   ├── MainViewModel.kt   # 主ViewModel
│   │   │   └── RuleViewModel.kt   # 规则ViewModel(新增)
│   │   ├── MainActivity.kt        # 主Activity
│   │   └── PlayerActivity.kt      # 播放器Activity
│   └── res/                       # 资源文件
└── build.gradle.kts
```

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK API 34

### 构建命令
```bash
# 构建Debug APK
./gradlew assembleDebug

# 构建Release APK
./gradlew assembleRelease
```

构建产物位置：
- Debug APK: `app/build/outputs/apk/debug/`
- Release APK: `app/build/outputs/apk/release/`

### GitHub Actions 自动构建
项目已配置 `.github/workflows/build-android-apk.yml` 工作流：
- 推送代码到 main/master 分支自动构建
- 创建 tag (v*) 自动发布 Release
- 构建产物包含：Debug APK、Release APK、完整源码

## 应用截图

应用包含以下页面设计：
1. 首页 - 推荐内容展示
2. 分类页 - 多维度筛选
3. 追剧页 - 追剧进度管理
4. 我的页 - 个人中心
5. 播放器 - 视频播放详情
6. 搜索页 - 搜索与热门榜单
7. 片单页 - 精选片单
8. 设置页 - 播放偏好设置
9. 关于页 - 版本与协议
