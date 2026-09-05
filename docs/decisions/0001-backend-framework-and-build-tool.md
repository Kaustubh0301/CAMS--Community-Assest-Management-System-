# ADR-0001 — Backend framework, runtime, and build tool

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-01**.
**Date:** 2026-09-04 · **Review note:** approved as written; JDK 21 LTS is the target even though JDK 24 is the machine's currently-installed JDK (install is a later environment task, not done now).
**Relates to:** REQUIREMENTS.md D5, D26, D36; SYSTEM_ARCHITECTURE.md §9; team decision **AD-01**

## Context

Requirements fix the client (Flutter), platform (Android API 26+), and database
(PostgreSQL, D36). The **backend framework and build tool are still PROPOSED**
(PLAN.md §3). The backend must be buildable, testable, demonstrable, and
*explainable* by a **six-member student team** within roughly one year, and must
support: PostgreSQL access, RBAC + auth, REST APIs, multipart file upload,
PDF + Excel generation, automated testing, and simple single-host deployment.

The environment inspection found **JDK 24 installed** (non-LTS), no Maven/Gradle,
no Flutter, no PostgreSQL.

## Decision

1. **Backend framework: Java + Spring Boot 3.x.**
2. **Runtime: JDK 21 (LTS)** — not JDK 24. Pin via the build and document in
   `TASKS.md` §1.
3. **Build tool: Maven.**
4. **Supporting libraries (proposed):** Spring Web (REST), Spring Data JPA +
   Hibernate (PostgreSQL), Spring Security (auth/RBAC), Flyway (SQL migrations),
   Apache POI (Excel), OpenPDF or an HTML→PDF approach (PDF — see ADR-0011),
   JUnit 5 + Spring MockMvc + Testcontainers-PostgreSQL (tests).
5. **Frontend note (not the subject of this ADR but recorded here):** the Flutter
   app picks **one** state-management approach (e.g. Riverpod *or* Bloc) and
   records it; this ADR does not choose it.

## Alternatives considered

| Option | Why not (for this team/project) |
|--------|--------------------------------|
| **Node.js + NestJS/Express** | Viable and light. NestJS adds its own DI/decorators learning curve; Express needs many hand-picked libs (auth, validation, ORM) the team would have to integrate and secure themselves. Weaker "batteries-included" story for security + Excel/PDF than Spring. |
| **Python + Django REST Framework** | Strong batteries-included, good admin. But the team's installed toolchain is Java; Django's ORM/migrations + DRF are a second ecosystem to learn; packaging/deploy less uniform than a single JAR. |
| **Python + FastAPI** | Excellent DX, but you assemble auth/ORM/migrations/admin yourself; async model is extra concept load; smaller "one obvious way" for a student team. |
| **ASP.NET Core (C#)** | Technically excellent and batteries-included. Rejected only because it introduces a different language/runtime from the installed JDK and typical university Java coursework; no functional advantage here. |
| **Spring Boot + Kotlin** | Fine, but adds a language on top of the framework; keep Java to minimise concept load. |
| **Build tool: Gradle** | Good, faster incremental builds, Kotlin DSL. Rejected as the default because Maven's XML is more uniform across the vast majority of Spring tutorials/docs a student team will follow; fewer "why is my build script different" moments. Gradle is an acceptable substitute if the team already knows it. |
| **JDK 24** | Non-LTS, ~6-month support; some libraries/tools (Lombok, older plugins, static analysers) lag. LTS 21 is the low-friction choice for a year-long project. |

## Consequences

**Positive**
- One language (Java) across build, tests, and (conceptually familiar) tooling;
  matches typical coursework.
- Spring Security, Spring Data JPA, validation, and Boot auto-config remove a lot
  of glue code and common security mistakes.
- Single executable JAR → trivial single-host deploy (SYSTEM_ARCHITECTURE §10).
- Testcontainers gives real-PostgreSQL integration tests → satisfies
  NFR-MAINT-001 credibly.
- Large, stable documentation base for a student team.

**Negative / trade-offs**
- Spring has a learning curve (context, starters, JPA lazy-loading pitfalls);
  mitigated by keeping the app a plain modular monolith (ADR-0002) and avoiding
  advanced features.
- JVM memory footprint higher than Node/Python on a tiny host; acceptable for a
  single-host MVP.
- Requires installing JDK 21 + Maven (currently missing) — tracked in `TASKS.md`
  §1, not solved here.
- Choosing Maven means slower clean builds than Gradle; negligible at this size.

**Neutral**
- If the team has strong prior Node/Python skill and weak Java skill, revisiting
  this ADR before implementation is legitimate — record a superseding ADR.
