// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'category_controller.dart';

// **************************************************************************
// StoreGenerator
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, unnecessary_brace_in_string_interps, unnecessary_lambdas, prefer_expression_function_bodies, lines_longer_than_80_chars, avoid_as, avoid_annotating_with_dynamic, no_leading_underscores_for_local_identifiers

mixin _$CategoryController on _CategoryController, Store {
  late final _$selectedContentTypeAtom =
      Atom(name: '_CategoryController.selectedContentType', context: context);

  @override
  int get selectedContentType {
    _$selectedContentTypeAtom.reportRead();
    return super.selectedContentType;
  }

  @override
  set selectedContentType(int value) {
    _$selectedContentTypeAtom.reportWrite(value, super.selectedContentType, () {
      super.selectedContentType = value;
    });
  }

  late final _$selectedGenreAtom =
      Atom(name: '_CategoryController.selectedGenre', context: context);

  @override
  int get selectedGenre {
    _$selectedGenreAtom.reportRead();
    return super.selectedGenre;
  }

  @override
  set selectedGenre(int value) {
    _$selectedGenreAtom.reportWrite(value, super.selectedGenre, () {
      super.selectedGenre = value;
    });
  }

  late final _$selectedRegionAtom =
      Atom(name: '_CategoryController.selectedRegion', context: context);

  @override
  int get selectedRegion {
    _$selectedRegionAtom.reportRead();
    return super.selectedRegion;
  }

  @override
  set selectedRegion(int value) {
    _$selectedRegionAtom.reportWrite(value, super.selectedRegion, () {
      super.selectedRegion = value;
    });
  }

  late final _$selectedYearAtom =
      Atom(name: '_CategoryController.selectedYear', context: context);

  @override
  int get selectedYear {
    _$selectedYearAtom.reportRead();
    return super.selectedYear;
  }

  @override
  set selectedYear(int value) {
    _$selectedYearAtom.reportWrite(value, super.selectedYear, () {
      super.selectedYear = value;
    });
  }

  late final _$selectedSortAtom =
      Atom(name: '_CategoryController.selectedSort', context: context);

  @override
  int get selectedSort {
    _$selectedSortAtom.reportRead();
    return super.selectedSort;
  }

  @override
  set selectedSort(int value) {
    _$selectedSortAtom.reportWrite(value, super.selectedSort, () {
      super.selectedSort = value;
    });
  }

  late final _$categoryListAtom =
      Atom(name: '_CategoryController.categoryList', context: context);

  @override
  ObservableList<BangumiItem> get categoryList {
    _$categoryListAtom.reportRead();
    return super.categoryList;
  }

  @override
  set categoryList(ObservableList<BangumiItem> value) {
    _$categoryListAtom.reportWrite(value, super.categoryList, () {
      super.categoryList = value;
    });
  }

  late final _$isLoadingAtom =
      Atom(name: '_CategoryController.isLoading', context: context);

  @override
  bool get isLoading {
    _$isLoadingAtom.reportRead();
    return super.isLoading;
  }

  @override
  set isLoading(bool value) {
    _$isLoadingAtom.reportWrite(value, super.isLoading, () {
      super.isLoading = value;
    });
  }

  late final _$isTimeOutAtom =
      Atom(name: '_CategoryController.isTimeOut', context: context);

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

  late final _$queryCategoryListAsyncAction =
      AsyncAction('_CategoryController.queryCategoryList', context: context);

  @override
  Future<void> queryCategoryList() {
    return _$queryCategoryListAsyncAction.run(() => super.queryCategoryList());
  }

  late final _$_CategoryControllerActionController =
      ActionController(name: '_CategoryController', context: context);

  @override
  void setContentType(int index) {
    final _$actionInfo = _$_CategoryControllerActionController.startAction(
        name: '_CategoryController.setContentType');
    try {
      return super.setContentType(index);
    } finally {
      _$_CategoryControllerActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setGenre(int index) {
    final _$actionInfo = _$_CategoryControllerActionController.startAction(
        name: '_CategoryController.setGenre');
    try {
      return super.setGenre(index);
    } finally {
      _$_CategoryControllerActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setRegion(int index) {
    final _$actionInfo = _$_CategoryControllerActionController.startAction(
        name: '_CategoryController.setRegion');
    try {
      return super.setRegion(index);
    } finally {
      _$_CategoryControllerActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setYear(int index) {
    final _$actionInfo = _$_CategoryControllerActionController.startAction(
        name: '_CategoryController.setYear');
    try {
      return super.setYear(index);
    } finally {
      _$_CategoryControllerActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setSort(int index) {
    final _$actionInfo = _$_CategoryControllerActionController.startAction(
        name: '_CategoryController.setSort');
    try {
      return super.setSort(index);
    } finally {
      _$_CategoryControllerActionController.endAction(_$actionInfo);
    }
  }

  @override
  String toString() {
    return '''
selectedContentType: ${selectedContentType},
selectedGenre: ${selectedGenre},
selectedRegion: ${selectedRegion},
selectedYear: ${selectedYear},
selectedSort: ${selectedSort},
categoryList: ${categoryList},
isLoading: ${isLoading},
isTimeOut: ${isTimeOut}
    ''';
  }
}
