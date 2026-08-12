import 'package:flutter_modular/flutter_modular.dart';
import 'package:kazumi/pages/category/category_controller.dart';
import 'package:kazumi/pages/category/category_page.dart';

final categoryModule = createModule(
  path: '/category',
  register: (c) {
    c
      ..addSingleton<CategoryController>(CategoryController.new)
      ..route(
        '/',
        transition: TransitionType.none,
        child: (context, state) => CategoryPage(
          controller: inject<CategoryController>(),
        ),
      );
  },
);
