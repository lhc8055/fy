import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_modular/flutter_modular.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:mobx/mobx.dart';
import 'package:kazumi/pages/popular/popular_controller.dart';
import 'package:kazumi/bean/card/network_img_layer.dart';
import 'package:kazumi/modules/bangumi/bangumi_item.dart';
import 'package:kazumi/utils/constants.dart';

/// 首页 - 视频流媒体风格新版首页
class PopularPage extends StatefulWidget {
  const PopularPage({
    super.key,
    required this.controller,
  });

  final PopularController controller;

  @override
  State<PopularPage> createState() => _PopularPageState();
}

class _PopularPageState extends State<PopularPage> {
  /// 主纵向滚动控制器
  late final ScrollController scrollController;

  /// 轮播图控制器
  late final PageController bannerController;

  /// 轮播图自动播放定时器
  Timer? _bannerTimer;

  /// 当前轮播图索引
  int _currentBannerIndex = 0;

  /// 当前选中的顶部分类索引
  int _currentCategoryIndex = 0;

  /// 顶部分类标签
  static const List<String> _categories = ['推荐', '电影', '电视剧', '动漫', '综艺'];

  /// 主题强调色（与设计稿一致的蓝色）
  static const Color _accentBlue = Color(0xFF2B8BFF);

  /// 水平内边距
  static const double _horizontalPadding = 12.0;

  PopularController get popularController => widget.controller;

  @override
  void initState() {
    super.initState();
    scrollController = ScrollController();
    bannerController = PageController();
    if (popularController.trendList.isEmpty) {
      popularController.initHomePage();
    }
    _startBannerAutoAdvance();
  }

  @override
  void dispose() {
    _bannerTimer?.cancel();
    bannerController.dispose();
    scrollController.dispose();
    super.dispose();
  }

  /// 启动轮播图自动轮播（每 4 秒切换一次）
  void _startBannerAutoAdvance() {
    _bannerTimer?.cancel();
    _bannerTimer = Timer.periodic(const Duration(seconds: 4), (_) {
      final int count = popularController.bannerList.length;
      if (count <= 1 || !bannerController.hasClients) return;
      final int next = (_currentBannerIndex + 1) % count;
      bannerController.animateToPage(
        next,
        duration: const Duration(milliseconds: 400),
        curve: Curves.easeInOut,
      );
    });
  }

  /// 卡片宽度：手机端约 120，宽屏适当放大
  double get _cardWidth {
    final double screenWidth = MediaQuery.of(context).size.width;
    if (screenWidth > 600) return 150;
    return 120;
  }

  /// 从 airDate 中解析年份作为角标文案
  String _extractYear(String airDate) {
    if (airDate.isEmpty) return '';
    final List<String> parts = airDate.split('-');
    if (parts.isNotEmpty && parts[0].length == 4) {
      return parts[0];
    }
    return airDate.length >= 4 ? airDate.substring(0, 4) : airDate;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: SafeArea(
        child: CustomScrollView(
          controller: scrollController,
          slivers: [
            SliverToBoxAdapter(child: _buildCategoryTabs()),
            SliverToBoxAdapter(child: _buildSearchBar()),
            SliverToBoxAdapter(child: _buildBannerCarousel()),
            SliverToBoxAdapter(
              child: _buildSection(
                title: '正在热播',
                icon: Icons.local_fire_department,
                iconColor: Colors.orange,
                list: popularController.trendList,
              ),
            ),
            SliverToBoxAdapter(
              child: _buildSection(
                title: '猜你喜欢',
                icon: Icons.favorite,
                iconColor: Colors.pinkAccent,
                list: popularController.recommendedList,
                showRecommendBadge: true,
              ),
            ),
            SliverToBoxAdapter(
              child: _buildSection(
                title: '热门电影',
                icon: Icons.movie,
                iconColor: _accentBlue,
                list: popularController.movieList,
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
          ],
        ),
      ),
    );
  }

  // ---------------------------------------------------------------------------
  // 顶部分类标签栏
  // ---------------------------------------------------------------------------
  Widget _buildCategoryTabs() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 8, 4, 4),
      child: Row(
        children: [
          Expanded(
            child: SizedBox(
              height: 44,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: EdgeInsets.zero,
                itemCount: _categories.length,
                separatorBuilder: (_, __) => const SizedBox(width: 22),
                itemBuilder: (BuildContext context, int index) {
                  final bool active = index == _currentCategoryIndex;
                  return GestureDetector(
                    onTap: () {
                      if (_currentCategoryIndex == index) return;
                      setState(() {
                        _currentCategoryIndex = index;
                      });
                    },
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: <Widget>[
                        Text(
                          _categories[index],
                          style: TextStyle(
                            fontSize: active ? 18 : 15,
                            fontWeight:
                                active ? FontWeight.bold : FontWeight.normal,
                            color: active
                                ? _accentBlue
                                : Theme.of(context).colorScheme.onSurfaceVariant,
                          ),
                        ),
                        const SizedBox(height: 5),
                        Container(
                          width: 22,
                          height: 3,
                          decoration: BoxDecoration(
                            color: active ? _accentBlue : Colors.transparent,
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
          ),
          IconButton(
            tooltip: '历史记录',
            onPressed: () => context.pushNamed('/settings/history/'),
            icon: const Icon(Icons.history, size: 22),
          ),
        ],
      ),
    );
  }

  // ---------------------------------------------------------------------------
  // 搜索栏
  // ---------------------------------------------------------------------------
  Widget _buildSearchBar() {
    final Color barColor = Theme.of(context)
        .colorScheme
        .onInverseSurface
        .withValues(alpha: 0.5);
    final Color iconColor = Theme.of(context).colorScheme.onSurfaceVariant;
    return Padding(
      padding: const EdgeInsets.fromLTRB(
          _horizontalPadding, 4, _horizontalPadding, 12),
      child: GestureDetector(
        onTap: () => context.pushNamed('/search/'),
        child: Container(
          height: 44,
          decoration: BoxDecoration(
            color: barColor,
            borderRadius: BorderRadius.circular(22),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 14),
          child: Row(
            children: <Widget>[
              Icon(Icons.search, size: 20, color: iconColor),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '搜索影片、演员、导演',
                  style: TextStyle(
                    fontSize: 14,
                    color: iconColor,
                  ),
                ),
              ),
              Icon(Icons.download_outlined, size: 20, color: iconColor),
              const SizedBox(width: 14),
              Icon(Icons.tune, size: 20, color: iconColor),
            ],
          ),
        ),
      ),
    );
  }

  // ---------------------------------------------------------------------------
  // 顶部轮播图
  // ---------------------------------------------------------------------------
  Widget _buildBannerCarousel() {
    final double screenWidth = MediaQuery.of(context).size.width;
    final double bannerWidth = screenWidth - _horizontalPadding * 2;
    final double bannerHeight = bannerWidth * 9 / 16;

    return Observer(
      builder: (_) {
        final ObservableList<BangumiItem> banners =
            popularController.bannerList;

        if (banners.isEmpty) {
          return Padding(
            padding: const EdgeInsets.fromLTRB(
                _horizontalPadding, 0, _horizontalPadding, 12),
            child: AspectRatio(
              aspectRatio: 16 / 9,
              child: Container(
                decoration: BoxDecoration(
                  color: Theme.of(context)
                      .colorScheme
                      .onInverseSurface
                      .withValues(alpha: 0.4),
                  borderRadius: BorderRadius.circular(StyleString.imgRadius.x),
                ),
              ),
            ),
          );
        }

        return Padding(
          padding: const EdgeInsets.fromLTRB(
              _horizontalPadding, 0, _horizontalPadding, 12),
          child: AspectRatio(
            aspectRatio: 16 / 9,
            child: Stack(
              children: <Widget>[
                ClipRRect(
                  borderRadius: BorderRadius.circular(StyleString.imgRadius.x),
                  child: PageView.builder(
                    controller: bannerController,
                    itemCount: banners.length,
                    onPageChanged: (int index) {
                      setState(() {
                        _currentBannerIndex = index;
                      });
                    },
                    itemBuilder: (BuildContext context, int index) {
                      final BangumiItem item = banners[index];
                      final String imageUrl = item.images['large'] ?? '';
                      final String title =
                          item.nameCn.isNotEmpty ? item.nameCn : item.name;
                      return GestureDetector(
                        onTap: () =>
                            context.pushNamed('/info/', arguments: item),
                        child: Stack(
                          fit: StackFit.expand,
                          children: <Widget>[
                            NetworkImgLayer(
                              src: imageUrl,
                              width: bannerWidth,
                              height: bannerHeight,
                            ),
                            Positioned(
                              left: 0,
                              right: 0,
                              bottom: 0,
                              child: IgnorePointer(
                                child: Container(
                                  padding: const EdgeInsets.fromLTRB(
                                      14, 32, 14, 14),
                                  decoration: BoxDecoration(
                                    gradient: LinearGradient(
                                      begin: Alignment.topCenter,
                                      end: Alignment.bottomCenter,
                                      colors: <Color>[
                                        Colors.transparent,
                                        Colors.black.withValues(alpha: 0.65),
                                      ],
                                    ),
                                  ),
                                  child: Text(
                                    title,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 15,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                ),
                // 右下角圆点指示器
                Positioned(
                  right: 12,
                  bottom: 10,
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: List<Widget>.generate(banners.length,
                        (int i) {
                      final bool active = i == _currentBannerIndex;
                      return AnimatedContainer(
                        duration: const Duration(milliseconds: 250),
                        width: active ? 18 : 6,
                        height: 6,
                        margin: const EdgeInsets.symmetric(horizontal: 2),
                        decoration: BoxDecoration(
                          color: active
                              ? _accentBlue
                              : Colors.white.withValues(alpha: 0.6),
                          borderRadius: BorderRadius.circular(3),
                        ),
                      );
                    }),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  // ---------------------------------------------------------------------------
  // 通用板块（正在热播 / 猜你喜欢 / 热门电影）
  // ---------------------------------------------------------------------------
  Widget _buildSection({
    required String title,
    required IconData icon,
    required Color iconColor,
    required ObservableList<BangumiItem> list,
    bool showRecommendBadge = false,
  }) {
    final double cardWidth = _cardWidth;
    // 海报高度（2:3 竖版） + 标题区域
    final double sectionHeight = cardWidth * 1.5 + 44;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        _buildSectionHeader(title: title, icon: icon, iconColor: iconColor),
        SizedBox(height: StyleString.cardSpace),
        Observer(
          builder: (_) {
            if (list.isEmpty) {
              return _buildSkeletonRow(sectionHeight);
            }
            return SizedBox(
              height: sectionHeight,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding:
                    const EdgeInsets.symmetric(horizontal: _horizontalPadding),
                itemCount: list.length,
                separatorBuilder: (_, __) => const SizedBox(width: 10),
                itemBuilder: (BuildContext context, int index) {
                  return _buildPosterCard(
                    list[index],
                    showRecommendBadge:
                        showRecommendBadge && index % 3 == 0,
                  );
                },
              ),
            );
          },
        ),
        const SizedBox(height: 20),
      ],
    );
  }

  Widget _buildSectionHeader({
    required String title,
    required IconData icon,
    required Color iconColor,
  }) {
    final Color secondary =
        Theme.of(context).colorScheme.onSurfaceVariant;
    return Padding(
      padding: const EdgeInsets.fromLTRB(
          _horizontalPadding, 4, _horizontalPadding, 0),
      child: Row(
        children: <Widget>[
          Icon(icon, size: 20, color: iconColor),
          const SizedBox(width: 6),
          Text(
            title,
            style: const TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.bold,
            ),
          ),
          const Spacer(),
          GestureDetector(
            onTap: () {},
            child: Row(
              children: <Widget>[
                Text(
                  '更多',
                  style: TextStyle(fontSize: 13, color: secondary),
                ),
                Icon(
                  Icons.chevron_right,
                  size: 16,
                  color: secondary,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ---------------------------------------------------------------------------
  // 海报卡片
  // ---------------------------------------------------------------------------
  Widget _buildPosterCard(BangumiItem item, {bool showRecommendBadge = false}) {
    final double cardWidth = _cardWidth;
    final double posterHeight = cardWidth * 1.5; // 2:3 竖版海报
    final String imageUrl = item.images['large'] ?? '';
    final String title = item.nameCn.isNotEmpty ? item.nameCn : item.name;
    final String year = _extractYear(item.airDate);
    final bool hasRating = item.ratingScore > 0;

    return GestureDetector(
      onTap: () => context.pushNamed('/info/', arguments: item),
      child: SizedBox(
        width: cardWidth,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            SizedBox(
              width: cardWidth,
              height: posterHeight,
              child: Stack(
                fit: StackFit.expand,
                children: <Widget>[
                  NetworkImgLayer(
                    src: imageUrl,
                    width: cardWidth,
                    height: posterHeight,
                  ),
                  // 底部渐变蒙层（衬托角标）
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 0,
                    child: IgnorePointer(
                      child: Container(
                        height: 52,
                        decoration: BoxDecoration(
                          borderRadius: const BorderRadius.vertical(
                            bottom: StyleString.imgRadius,
                          ),
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: <Color>[
                              Colors.transparent,
                              Colors.black.withValues(alpha: 0.55),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ),
                  // 左下角：年份/集数角标
                  if (year.isNotEmpty)
                    Positioned(
                      left: 6,
                      bottom: 6,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 5, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.black.withValues(alpha: 0.55),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          year,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 10,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                    ),
                  // 右下角：评分角标（橙色）
                  if (hasRating)
                    Positioned(
                      right: 6,
                      bottom: 6,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 5, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.orange,
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          item.ratingScore.toStringAsFixed(1),
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  // 左上角：推荐角标（蓝色）
                  if (showRecommendBadge)
                    Positioned(
                      left: 0,
                      top: 0,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 7, vertical: 2),
                        decoration: BoxDecoration(
                          color: _accentBlue,
                          borderRadius: BorderRadius.only(
                            topLeft:
                                Radius.circular(StyleString.imgRadius.x),
                            bottomRight: Radius.circular(8),
                          ),
                        ),
                        child: const Text(
                          '推荐',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 6),
            SizedBox(
              height: 38,
              child: Text(
                title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  height: 1.2,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ---------------------------------------------------------------------------
  // 骨架占位（数据加载中）
  // ---------------------------------------------------------------------------
  Widget _buildSkeletonRow(double height) {
    final double cardWidth = _cardWidth;
    final Color skeletonColor = Theme.of(context)
        .colorScheme
        .onInverseSurface
        .withValues(alpha: 0.4);
    return SizedBox(
      height: height,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        physics: const NeverScrollableScrollPhysics(),
        padding: const EdgeInsets.symmetric(horizontal: _horizontalPadding),
        itemCount: 4,
        separatorBuilder: (_, __) => const SizedBox(width: 10),
        itemBuilder: (BuildContext context, int index) {
          return SizedBox(
            width: cardWidth,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: cardWidth,
                  height: cardWidth * 1.5,
                  decoration: BoxDecoration(
                    color: skeletonColor,
                    borderRadius:
                        BorderRadius.circular(StyleString.imgRadius.x),
                  ),
                ),
                const SizedBox(height: 6),
                Container(
                  width: cardWidth * 0.7,
                  height: 10,
                  decoration: BoxDecoration(
                    color: skeletonColor,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
