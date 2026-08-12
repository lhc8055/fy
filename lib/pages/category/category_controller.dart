import 'package:kazumi/modules/bangumi/bangumi_item.dart';
import 'package:kazumi/request/apis/bangumi_api.dart';
import 'package:kazumi/services/storage/storage.dart';
import 'package:mobx/mobx.dart';

part 'category_controller.g.dart';

class CategoryController = _CategoryController with _$CategoryController;

abstract class _CategoryController with Store {
  // Content type tabs: 0=电影, 1=电视剧, 2=综艺, 3=动漫
  @observable
  int selectedContentType = 0;

  // Filter state: genre, region, year
  @observable
  int selectedGenre = 0; // 0=全部, 1=剧情, 2=喜剧, 3=动作, 4=爱情, 5=科幻

  @observable
  int selectedRegion = 0; // 0=全部, 1=大陆, 2=香港, 3=台湾, 4=美国, 5=韩国

  @observable
  int selectedYear = 0; // 0=全部, 1=2024, 2=2023, 3=2022, 4=2021, 5=2020

  // Sort: 0=最多播放, 1=最新上映, 2=评分最高
  @observable
  int selectedSort = 0;

  // Content list
  @observable
  ObservableList<BangumiItem> categoryList = ObservableList<BangumiItem>();

  @observable
  bool isLoading = false;

  @observable
  bool isTimeOut = false;

  // Static filter data
  final List<String> contentTypes = ['电影', '电视剧', '综艺', '动漫'];
  final List<String> genres = ['全部', '剧情', '喜剧', '动作', '爱情', '科幻'];
  final List<String> regions = ['全部', '大陆', '香港', '台湾', '美国', '韩国'];
  final List<String> years = ['全部', '2024', '2023', '2022', '2021', '2020'];
  final List<String> sortOptions = ['最多播放', '最新上映', '评分最高'];

  /// Number of subjects requested per category query.
  static const int _pageSize = 50;

  bool get _bangumiMirrorEnabled =>
      GStorage.getSetting(SettingsKeys.enableBangumiProxy);

  /// Map content type to Bangumi subject type.
  /// 0=电影, 1=电视剧, 2=综艺 -> type 6 (三次元); 3=动漫 -> type 2 (动画)
  int get _bangumiType => selectedContentType == 3 ? 2 : 6;

  // Async actions commit each segment between awaits as one transaction, so
  // clear+addAll never shows observers an intermediate empty list.
  @action
  Future<void> queryCategoryList() async {
    isLoading = true;
    isTimeOut = false;
    categoryList.clear();
    try {
      final int type = _bangumiType;

      // Build genre tag from the genre filter.
      final String tag = selectedGenre > 0 ? genres[selectedGenre] : '';

      List<BangumiItem> items = [];
      if (_bangumiMirrorEnabled) {
        // The mirror endpoint supports tag filtering but not subject type,
        // so the type is narrowed down client-side afterwards.
        items = await BangumiApi.getBangumiMirrorPopularSubjects(
          tag: tag,
          limit: _pageSize,
          offset: 0,
        );
        items = items.where((item) => item.type == type).toList();
      } else {
        // The official trends endpoint is the only one that supports
        // filtering by subject type.
        items = await BangumiApi.getBangumiTrendsList(
          type: type,
          limit: _pageSize,
          offset: 0,
        );
        // The trends endpoint has no tag parameter, so filter the genre
        // client-side using the subject tags.
        if (tag.isNotEmpty) {
          items = items
              .where((item) => item.tags.any((t) => t.name == tag))
              .toList();
        }
      }

      // Apply region filter client-side. Region names map to Bangumi tags.
      if (selectedRegion > 0) {
        final String region = regions[selectedRegion];
        items = items
            .where((item) => item.tags.any((t) => t.name == region))
            .toList();
      }

      // Apply year filter client-side using the air date string.
      if (selectedYear > 0) {
        final String yearStr = years[selectedYear];
        items =
            items.where((item) => item.airDate.contains(yearStr)).toList();
      }

      // Apply sorting.
      switch (selectedSort) {
        case 0:
          // 最多播放 - sort by votes (collection total) descending.
          items.sort((a, b) => b.votes.compareTo(a.votes));
          break;
        case 1:
          // 最新上映 - sort by air date descending.
          items.sort((a, b) => b.airDate.compareTo(a.airDate));
          break;
        case 2:
          // 评分最高 - sort by rating score descending.
          items.sort((a, b) => b.ratingScore.compareTo(a.ratingScore));
          break;
        default:
          break;
      }

      categoryList.addAll(items);
    } catch (e) {
      isTimeOut = true;
    } finally {
      isLoading = false;
    }
  }

  @action
  void setContentType(int index) {
    selectedContentType = index;
    queryCategoryList();
  }

  @action
  void setGenre(int index) {
    selectedGenre = index;
    queryCategoryList();
  }

  @action
  void setRegion(int index) {
    selectedRegion = index;
    queryCategoryList();
  }

  @action
  void setYear(int index) {
    selectedYear = index;
    queryCategoryList();
  }

  @action
  void setSort(int index) {
    selectedSort = index;
    queryCategoryList();
  }

  void init() {
    if (categoryList.isEmpty) {
      queryCategoryList();
    }
  }
}
