import 'package:flutter/material.dart';
import 'package:flutter_modular/flutter_modular.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:mobx/mobx.dart';
import 'package:kazumi/pages/category/category_controller.dart';
import 'package:kazumi/bean/card/network_img_layer.dart';
import 'package:kazumi/modules/bangumi/bangumi_item.dart';
import 'package:kazumi/utils/constants.dart';

/// 分类浏览页 - 按类型/地区/年份筛选并排序的视频列表
class CategoryPage extends StatefulWidget {
  const CategoryPage({
    super.key,
    required this.controller,
  });

  final CategoryController controller;

  @override
  State<CategoryPage> createState() => _CategoryPageState();
}

class _CategoryPageState extends State<CategoryPage> {
  CategoryController get controller => widget.controller;

  static const Color _accentBlue = Color(0xFF007AFF);
  static const Color _unselectedGray = Color(0xFF999999);
  static const double _horizontalPadding = 12.0;

  late final ScrollController _scrollController;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
    if (controller.categoryList.isEmpty) {
      controller.init();
    }
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  String _extractYear(String airDate) {
    if (airDate.isEmpty) return '';
    final parts = airDate.split('-');
    if (parts.isNotEmpty && parts[0].length == 4) return parts[0];
    return airDate.length >= 4 ? airDate.substring(0, 4) : airDate;
  }

  double get _cardWidth {
    final screenWidth = MediaQuery.of(context).size.width;
    return (screenWidth - 2 * _horizontalPadding - 2 * StyleString.cardSpace) / 3;
  }

  double get _cardAspectRatio {
    final cardWidth = _cardWidth;
    final posterHeight = cardWidth * 1.5;
    const titleArea = 42.0;
    return cardWidth / (posterHeight + titleArea);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildTitle(),
            _buildContentTypeTabs(),
            _buildFilterRow(
              label: '类型',
              options: controller.genres,
              selectedIndex: controller.selectedGenre,
              onSelected: controller.setGenre,
            ),
            _buildFilterRow(
              label: '地区',
              options: controller.regions,
              selectedIndex: controller.selectedRegion,
              onSelected: controller.setRegion,
            ),
            _buildFilterRow(
              label: '年份',
              options: controller.years,
              selectedIndex: controller.selectedYear,
              onSelected: controller.setYear,
            ),
            _buildSortRow(),
            Expanded(child: _buildContentArea()),
          ],
        ),
      ),
    );
  }

  Widget _buildTitle() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 12, _horizontalPadding, 4),
      child: const Text(
        '分类',
        style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.black87),
      ),
    );
  }

  Widget _buildContentTypeTabs() {
    return Observer(
      builder: (_) {
        return Padding(
          padding: const EdgeInsets.fromLTRB(_horizontalPadding, 4, _horizontalPadding, 4),
          child: Row(
            children: List.generate(controller.contentTypes.length, (index) {
              final active = index == controller.selectedContentType;
              return GestureDetector(
                onTap: () => controller.setContentType(index),
                child: Padding(
                  padding: const EdgeInsets.only(right: 24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        controller.contentTypes[index],
                        style: TextStyle(
                          fontSize: active ? 16 : 15,
                          fontWeight: active ? FontWeight.bold : FontWeight.normal,
                          color: active ? _accentBlue : Colors.black87,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Container(
                        width: 24,
                        height: 3,
                        decoration: BoxDecoration(
                          color: active ? _accentBlue : Colors.transparent,
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }),
          ),
        );
      },
    );
  }

  Widget _buildFilterRow({
    required String label,
    required List<String> options,
    required int selectedIndex,
    required ValueChanged<int> onSelected,
  }) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 2, _horizontalPadding, 2),
      child: SizedBox(
        height: 36,
        child: Row(
          children: [
            SizedBox(
              width: 36,
              child: Text(label, style: const TextStyle(fontSize: 13, color: _unselectedGray)),
            ),
            Expanded(
              child: Observer(
                builder: (_) => ListView.separated(
                  scrollDirection: Axis.horizontal,
                  padding: EdgeInsets.zero,
                  itemCount: options.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 16),
                  itemBuilder: (context, index) {
                    final active = index == selectedIndex;
                    return GestureDetector(
                      onTap: () => onSelected(index),
                      child: Center(
                        child: Text(
                          options[index],
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: active ? FontWeight.bold : FontWeight.normal,
                            color: active ? _accentBlue : _unselectedGray,
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSortRow() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 4, _horizontalPadding, 8),
      child: SizedBox(
        height: 36,
        child: Observer(
          builder: (_) => Row(
            children: List.generate(controller.sortOptions.length, (index) {
              final active = index == controller.selectedSort;
              return GestureDetector(
                onTap: () => controller.setSort(index),
                child: Padding(
                  padding: EdgeInsets.only(right: index < controller.sortOptions.length - 1 ? 20 : 0),
                  child: Text(
                    controller.sortOptions[index],
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: active ? FontWeight.bold : FontWeight.normal,
                      color: active ? Colors.black87 : _unselectedGray,
                    ),
                  ),
                ),
              );
            }),
          ),
        ),
      ),
    );
  }

  Widget _buildContentArea() {
    return Observer(
      builder: (_) {
        final list = controller.categoryList;
        if (controller.isLoading && list.isEmpty) {
          return const Center(child: CircularProgressIndicator());
        }
        if (list.isEmpty) {
          return _buildSkeletonGrid();
        }
        return _buildGrid(list);
      },
    );
  }

  Widget _buildGrid(ObservableList<BangumiItem> list) {
    return GridView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 4, _horizontalPadding, 12),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: StyleString.cardSpace,
        mainAxisSpacing: StyleString.cardSpace,
        childAspectRatio: _cardAspectRatio,
      ),
      itemCount: list.length,
      itemBuilder: (context, index) => _buildPosterCard(list[index]),
    );
  }

  Widget _buildPosterCard(BangumiItem item) {
    final cardWidth = _cardWidth;
    final posterHeight = cardWidth * 1.5;
    final imageUrl = item.images['large'] ?? '';
    final title = item.nameCn.isNotEmpty ? item.nameCn : item.name;
    final year = _extractYear(item.airDate);
    final hasRating = item.ratingScore > 0;

    return GestureDetector(
      onTap: () => context.pushNamed('/info/', arguments: item),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: cardWidth,
            height: posterHeight,
            child: Stack(
              fit: StackFit.expand,
              children: [
                NetworkImgLayer(src: imageUrl, width: cardWidth, height: posterHeight),
                if (hasRating)
                  Positioned(
                    right: 6,
                    bottom: 6,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.black.withValues(alpha: 0.6),
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
              ],
            ),
          ),
          const SizedBox(height: 4),
          Text(
            title,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500, color: Colors.black87),
          ),
          if (year.isNotEmpty)
            Text(
              year,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 11, color: _unselectedGray),
            ),
        ],
      ),
    );
  }

  Widget _buildSkeletonGrid() {
    final cardWidth = _cardWidth;
    final posterHeight = cardWidth * 1.5;
    final skeletonColor = Theme.of(context).colorScheme.onInverseSurface.withValues(alpha: 0.4);

    return GridView.builder(
      padding: const EdgeInsets.fromLTRB(_horizontalPadding, 4, _horizontalPadding, 12),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: StyleString.cardSpace,
        mainAxisSpacing: StyleString.cardSpace,
        childAspectRatio: _cardAspectRatio,
      ),
      itemCount: 9,
      physics: const NeverScrollableScrollPhysics(),
      itemBuilder: (context, index) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: cardWidth,
              height: posterHeight,
              decoration: BoxDecoration(
                color: skeletonColor,
                borderRadius: BorderRadius.circular(StyleString.imgRadius.x),
              ),
            ),
            const SizedBox(height: 4),
            Container(
              width: cardWidth * 0.7,
              height: 10,
              decoration: BoxDecoration(color: skeletonColor, borderRadius: BorderRadius.circular(4)),
            ),
          ],
        );
      },
    );
  }
}
