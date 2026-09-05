# CLAUDE.md — How to work on CAMS

CAMS (Community Asset Management System) is a one-year university engineering
project. This file is intentionally short. It defines **how to operate**, not what
to build.

## Operating rules

1. **Code and tests are the source of truth.** When documentation, memory, or the
   conversation disagree with the code, the code wins — investigate before acting.
2. **Do not silently change approved architecture.** Approved decisions live in
   `PLAN.md` and `docs/decisions/`. Changing one requires a new recorded decision.
3. **On conflicting evidence, stop.** If something you discover contradicts an
   approved decision, halt and explain the conflict before making any change.
4. **Project state lives in files, not chat.** Keep `PLAN.md`, `TASKS.md`,
   `memory/MEMORY.md`, and `docs/` current. Do not rely on conversation history to
   carry state between sessions.
5. **Before implementing a feature:** inspect the relevant existing code,
   documentation, memory, and tests first.
6. **After implementing a change:** run or add tests for it, then update the
   affected project-state files (`TASKS.md`, `memory/MEMORY.md`, relevant `docs/`).
7. **Do not invent requirements.** If a requirement is missing or ambiguous, record
   it as an open question and ask.

## Where things live

| File / dir | Purpose |
|------------|---------|
| `PLAN.md` | High-level plan; separates proposed from approved decisions |
| `TASKS.md` | Task list and status |
| `memory/MEMORY.md` | Concise index of persistent project knowledge |
| `docs/` | requirements, architecture, database, api, testing, decisions |
