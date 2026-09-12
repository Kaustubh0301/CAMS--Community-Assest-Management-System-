import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:mobile/main.dart';

void main() {
  testWidgets('CamsApp boots and shows the app shell', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const ProviderScope(child: CamsApp()));

    expect(find.text('CAMS'), findsOneWidget);
    expect(find.text('Community Asset Management System'), findsOneWidget);
  });
}
