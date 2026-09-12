import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Phase 1 skeleton entry point. No CAMS screens or business logic yet —
/// this only proves the app boots with the approved Riverpod + Material 3
/// foundation (ADR-0018).
void main() {
  runApp(const ProviderScope(child: CamsApp()));
}

class CamsApp extends StatelessWidget {
  const CamsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CAMS',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: const HomeShell(),
    );
  }
}

class HomeShell extends ConsumerWidget {
  const HomeShell({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('CAMS')),
      body: const Center(
        child: Text('Community Asset Management System'),
      ),
    );
  }
}
