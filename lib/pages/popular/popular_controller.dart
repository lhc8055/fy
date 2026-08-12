import 'dart:math';
import 'package:kazumi/request/apis/bangumi_api.dart';
import 'package:kazumi/modules/bangumi/bangumi_item.dart';
import 'package:kazumi/services/storage/storage.dart';
import 'package:mobx/mobx.dart';

part 'popular_controller.g.dart';

class PopularController = _PopularController with _$PopularController;

abstract class _PopularController with Store {
  static const int _trendPageSize = 24;

  /// Number of items displayed in the homepage hero banner carousel.
  static const int _bannerLimit = 5;

  /// Tag used to curate the "猜你喜欢" (recommended for you) section.
  static const String _recommendedTag = '治愈';

  int _trendOffset = 0;

  @observable
  String currentTag = '';

  @observable
  ObservableList<BangumiItem> bangumiList = ObservableList.of([]);

  @observable
  ObservableList<BangumiItem> trendList = ObservableList.of([]);

  /// Top trending items shown in the hero banner carousel.
  @observable
  ObservableList<BangumiItem> bannerList = ObservableList.of([]);

  /// Curated recommendations for the "猜你喜欢" section.
  @observable
  ObservableList<BangumiItem> recommendedList = ObservableList.of([]);

  /// Real-action / movie items (Bangumi type 6) for the "热门电影" section.
  @observable
  ObservableList<BangumiItem> movieList = ObservableList.of([]);

  double scrollOffset = 0.0;

  @observable
  bool isLoadingMore = false;

  @observable
  bool isTimeOut = false;

  bool get _bangumiMirrorEnabled =>
      GStorage.getSetting(SettingsKeys.enableBangumiProxy);

  void setCurrentTag(String s) {
    currentTag = s;
  }

  void clearBangumiList() {
    bangumiList.clear();
  }

  // Async actions commit each segment between awaits as one transaction,
  // batching the completion writes into a single notification.
  @action
  Future<void> queryBangumiByTrend({String type = 'add'}) async {
    if (type == 'init') {
      trendList.clear();
      _trendOffset = 0;
    }
    isLoadingMore = true;
    final result = _bangumiMirrorEnabled
        ? await BangumiApi.getBangumiMirrorPopularSubjects(
            limit: _trendPageSize,
            offset: _trendOffset,
          )
        : await BangumiApi.getBangumiTrendsList(
            limit: _trendPageSize,
            offset: _trendOffset,
          );
    if (result.isNotEmpty) {
      _trendOffset += _trendPageSize;
    }
    final existingIds = trendList.map((item) => item.id).toSet();
    trendList.addAll(result.where((item) => existingIds.add(item.id)));
    isLoadingMore = false;
    isTimeOut = trendList.isEmpty;
  }

  @action
  Future<void> queryBangumiByTag({String type = 'add'}) async {
    if (type == 'init') {
      bangumiList.clear();
    }
    isLoadingMore = true;
    var tag = currentTag;
    var result = _bangumiMirrorEnabled
        ? await BangumiApi.getBangumiMirrorPopularSubjects(
            tag: tag,
            offset: bangumiList.length,
          )
        : await BangumiApi.getBangumiList(
            rank: Random().nextInt(8000) + 1,
            tag: tag,
          );
    bangumiList.addAll(result);
    isLoadingMore = false;
    isTimeOut = bangumiList.isEmpty;
  }

  /// Fetches the top trending items for the homepage hero banner carousel.
  ///
  /// Uses the configured mirror endpoint when the bangumi proxy is enabled,
  /// otherwise falls back to the official trends API. Only the first
  /// [_bannerLimit] items are retained.
  @action
  Future<void> queryBanner() async {
    bannerList.clear();
    final result = _bangumiMirrorEnabled
        ? await BangumiApi.getBangumiMirrorPopularSubjects(
            limit: _bannerLimit,
            offset: 0,
          )
        : await BangumiApi.getBangumiTrendsList(
            limit: _bannerLimit,
            offset: 0,
          );
    bannerList.addAll(result.take(_bannerLimit));
  }

  /// Fetches recommended items using a curated tag for the "猜你喜欢" section.
  ///
  /// A randomized rank offset is used with the official API so that the
  /// recommendation list refreshes between visits.
  @action
  Future<void> queryRecommended() async {
    recommendedList.clear();
    final result = _bangumiMirrorEnabled
        ? await BangumiApi.getBangumiMirrorPopularSubjects(
            tag: _recommendedTag,
          )
        : await BangumiApi.getBangumiList(
            rank: Random().nextInt(8000) + 1,
            tag: _recommendedTag,
          );
    recommendedList.addAll(result);
  }

  /// Fetches real-action / movie items (Bangumi type 6) for the "热门电影"
  /// section.
  ///
  /// The trends API is used directly because it is the only endpoint that
  /// supports filtering by subject type.
  @action
  Future<void> queryMovies() async {
    movieList.clear();
    final result = await BangumiApi.getBangumiTrendsList(
      type: 6,
      limit: _trendPageSize,
      offset: 0,
    );
    movieList.addAll(result);
  }

  /// Initializes all homepage sections in parallel.
  ///
  /// Triggers [queryBanner], [queryRecommended] and [queryMovies]
  /// concurrently so the homepage can render as soon as possible.
  @action
  Future<void> initHomePage() async {
    await Future.wait([
      queryBanner(),
      queryRecommended(),
      queryMovies(),
    ]);
  }
}
