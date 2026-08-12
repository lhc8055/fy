// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'popular_controller.dart';

// **************************************************************************
// StoreGenerator
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, unnecessary_brace_in_string_interps, unnecessary_lambdas, prefer_expression_function_bodies, lines_longer_than_80_chars, avoid_as, avoid_annotating_with_dynamic, no_leading_underscores_for_local_identifiers

mixin _$PopularController on _PopularController, Store {
  late final _$currentTagAtom =
      Atom(name: '_PopularController.currentTag', context: context);

  @override
  String get currentTag {
    _$currentTagAtom.reportRead();
    return super.currentTag;
  }

  @override
  set currentTag(String value) {
    _$currentTagAtom.reportWrite(value, super.currentTag, () {
      super.currentTag = value;
    });
  }

  late final _$bangumiListAtom =
      Atom(name: '_PopularController.bangumiList', context: context);

  @override
  ObservableList<BangumiItem> get bangumiList {
    _$bangumiListAtom.reportRead();
    return super.bangumiList;
  }

  @override
  set bangumiList(ObservableList<BangumiItem> value) {
    _$bangumiListAtom.reportWrite(value, super.bangumiList, () {
      super.bangumiList = value;
    });
  }

  late final _$trendListAtom =
      Atom(name: '_PopularController.trendList', context: context);

  @override
  ObservableList<BangumiItem> get trendList {
    _$trendListAtom.reportRead();
    return super.trendList;
  }

  @override
  set trendList(ObservableList<BangumiItem> value) {
    _$trendListAtom.reportWrite(value, super.trendList, () {
      super.trendList = value;
    });
  }

  late final _$bannerListAtom =
      Atom(name: '_PopularController.bannerList', context: context);

  @override
  ObservableList<BangumiItem> get bannerList {
    _$bannerListAtom.reportRead();
    return super.bannerList;
  }

  @override
  set bannerList(ObservableList<BangumiItem> value) {
    _$bannerListAtom.reportWrite(value, super.bannerList, () {
      super.bannerList = value;
    });
  }

  late final _$recommendedListAtom =
      Atom(name: '_PopularController.recommendedList', context: context);

  @override
  ObservableList<BangumiItem> get recommendedList {
    _$recommendedListAtom.reportRead();
    return super.recommendedList;
  }

  @override
  set recommendedList(ObservableList<BangumiItem> value) {
    _$recommendedListAtom.reportWrite(value, super.recommendedList, () {
      super.recommendedList = value;
    });
  }

  late final _$movieListAtom =
      Atom(name: '_PopularController.movieList', context: context);

  @override
  ObservableList<BangumiItem> get movieList {
    _$movieListAtom.reportRead();
    return super.movieList;
  }

  @override
  set movieList(ObservableList<BangumiItem> value) {
    _$movieListAtom.reportWrite(value, super.movieList, () {
      super.movieList = value;
    });
  }

  late final _$isLoadingMoreAtom =
      Atom(name: '_PopularController.isLoadingMore', context: context);

  @override
  bool get isLoadingMore {
    _$isLoadingMoreAtom.reportRead();
    return super.isLoadingMore;
  }

  @override
  set isLoadingMore(bool value) {
    _$isLoadingMoreAtom.reportWrite(value, super.isLoadingMore, () {
      super.isLoadingMore = value;
    });
  }

  late final _$isTimeOutAtom =
      Atom(name: '_PopularController.isTimeOut', context: context);

  @override
  bool get isTimeOut {
    _$isTimeOutAtom.reportRead();
    return super.isTimeOut;
  }

  @override
  set isTimeOut(bool value) {
    _$isTimeOutAtom.reportWrite(value, super.isTimeOut, () {
      super.isTimeOut = value;
    });
  }

  late final _$queryBangumiByTrendAsyncAction =
      AsyncAction('_PopularController.queryBangumiByTrend', context: context);

  @override
  Future<void> queryBangumiByTrend({String type = 'add'}) {
    return _$queryBangumiByTrendAsyncAction
        .run(() => super.queryBangumiByTrend(type: type));
  }

  late final _$queryBangumiByTagAsyncAction =
      AsyncAction('_PopularController.queryBangumiByTag', context: context);

  @override
  Future<void> queryBangumiByTag({String type = 'add'}) {
    return _$queryBangumiByTagAsyncAction
        .run(() => super.queryBangumiByTag(type: type));
  }

  late final _$queryBannerAsyncAction =
      AsyncAction('_PopularController.queryBanner', context: context);

  @override
  Future<void> queryBanner() {
    return _$queryBannerAsyncAction.run(() => super.queryBanner());
  }

  late final _$queryRecommendedAsyncAction =
      AsyncAction('_PopularController.queryRecommended', context: context);

  @override
  Future<void> queryRecommended() {
    return _$queryRecommendedAsyncAction.run(() => super.queryRecommended());
  }

  late final _$queryMoviesAsyncAction =
      AsyncAction('_PopularController.queryMovies', context: context);

  @override
  Future<void> queryMovies() {
    return _$queryMoviesAsyncAction.run(() => super.queryMovies());
  }

  late final _$initHomePageAsyncAction =
      AsyncAction('_PopularController.initHomePage', context: context);

  @override
  Future<void> initHomePage() {
    return _$initHomePageAsyncAction.run(() => super.initHomePage());
  }

  @override
  String toString() {
    return '''
currentTag: ${currentTag},
bangumiList: ${bangumiList},
trendList: ${trendList},
bannerList: ${bannerList},
recommendedList: ${recommendedList},
movieList: ${movieList},
isLoadingMore: ${isLoadingMore},
isTimeOut: ${isTimeOut}
    ''';
  }
}
