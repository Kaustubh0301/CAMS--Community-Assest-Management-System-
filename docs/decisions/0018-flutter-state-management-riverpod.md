# ADR-0018 — Flutter state management: Riverpod

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-20**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md D5, D12, D27, NFR-USE-*, NFR-I18N-*;
SYSTEM_ARCHITECTURE.md §9, §17 (Area A); ADR-0001 (frontend note)

## Context

CAMS is one Flutter app used by four roles, Android-first (D5, D26), bilingual
EN/HI (D12), with a deliberately simple rural-friendly UI (D27). The architecture
review deferred the state-management choice to the team. The app needs: auth/session
state, role-based navigation, server-data fetching with loading/error states,
form state (asset registration, complaint, completion), a polled notification
badge, and language selection — all testable.

## Decision

**Use Riverpod for Flutter state management**, applied consistently across the app:

- Providers for auth/session, current user + role, language, and an API client.
- Async providers (`FutureProvider` / `AsyncNotifier`) for server reads with
  built-in loading/error/data states — matches the "specific, actionable errors"
  usability requirement (NFR-USE-005).
- `Notifier` / `AsyncNotifier` classes for screen/form state and for actions that
  mutate server state (complaint transitions, uploads).
- Providers are unit-testable by overriding dependencies (fake API client) — feeds
  the Flutter test requirement (NFR-MAINT-001).
- One documented pattern for the whole team (SYSTEM_ARCHITECTURE §17, Area A owns
  the conventions doc).

## Alternatives considered

| Option | Assessment | Why not chosen |
|--------|-----------|----------------|
| **Bloc / flutter_bloc** | Mature, explicit event→state, strong tooling. | More boilerplate (events, states, blocs) per feature; steeper for a first-time team on a tight schedule. A reasonable alternative if the team already knew it. |
| **Provider (plain)** | Simple, official. | Weaker async ergonomics; tends to grow ad-hoc patterns; less compile-time safety than Riverpod. |
| **setState / InheritedWidget only** | No dependency. | Doesn't scale to app-wide auth/session/data state; poor testability. |
| **GetX** | Fast to write. | Opinionated, mixes routing/DI/state, weaker testability and community-consensus concerns. |
| **MobX / Redux** | Viable. | Redux is verbose; MobX adds codegen; neither offers an advantage here over Riverpod. |

| Dimension | Riverpod |
|-----------|----------|
| Complexity | Low–Medium |
| Boilerplate | Low |
| Async handling | Strong (first-class `AsyncValue`) |
| Testability | Strong (provider overrides) |
| Team familiarity | Learnable quickly; large docs/community |
| Compile-time safety | Good |

## Consequences

**Positive**
- One consistent, testable pattern for auth, data-fetching, forms, and the
  notification poll.
- `AsyncValue` gives uniform loading/error/data handling → consistent UX and less
  bespoke error code.
- Dependency overrides make widget/unit tests straightforward.

**Negative / trade-offs**
- A learning curve for members new to Riverpod (providers, `ref`, `Notifier`);
  mitigated by a short conventions doc and a couple of reference screens.
- Riverpod idioms evolve between major versions — pin a version and follow one
  version's patterns.
- If the team collectively has strong Bloc experience and no Riverpod experience,
  revisiting this ADR before UI build is legitimate — record a superseding ADR.
