# ADR-0013 — Map / GIS approach

**Status:** **ACCEPTED (approach)** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-10**.
**Open sub-item:** the exact **tile provider / licensing / usage terms** is a separately tracked architecture item (not yet resolved).
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md D13, FR-GIS-001..004, NFR-COMPAT-*; SYSTEM_ARCHITECTURE.md §11;
DATA_MODEL.md §4.3

## Context

D13 fixes: assets store latitude/longitude and a Ward ID; the app shows an
interactive marker map; **no** PostGIS requirement and **no** official
ward-boundary polygons for MVP. The client is Flutter on Android (API 26+).

## Decision

- **Client-side map** in the Flutter app using an **OpenStreetMap-based mapping
  approach** (an OSM-compatible Flutter map widget).
- The **backend stores and returns plain `latitude` / `longitude` decimal degrees**
  plus `ward_id` on the asset. No server-side spatial math, no PostGIS, no
  geometry types for MVP.
- **Asset markers support filtering and lookup:** a lightweight marker endpoint
  (`GET /assets?view=map&bbox=…` + category/status/ward filters) returns
  `{id, lat, lng, category, status}`; tapping a marker calls `GET /assets/{id}`.
- **Bounded queries:** the marker endpoint is always bounding-box constrained and
  capped by a configurable `maxMarkers` (ties to ADR-0019).
- Location capture on create/edit uses device GPS or a draggable pin; the app
  sends the final lat/long.
- **Tile provider / licensing** — **OPEN, separately tracked.** Options: an OSM
  tile source with acceptable usage terms, a self/institute-hosted tile cache, or a
  commercial SDK if a key is available. This must be settled (with attribution /
  usage-policy compliance) before the map feature is built. Fallback if no
  acceptable tile source: a plain coordinate list + "open in device maps" link.
- This is **not** an external *integration* in the D20 sense — only map tiles are
  fetched to the device; no CAMS data leaves the system.

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **PostGIS + geometry columns** | D13 says not required; adds an extension, spatial indexing, and query complexity for points-only needs. Addable later (new columns/tables) if a Secondary feature needs polygons/radius search. |
| **Server-rendered map tiles / static map images** | Backend tile rendering or proxying is infra the team doesn't need; the device renders the map fine. |
| **Ward-boundary polygons in MVP** | Explicitly out of scope (D13); needs boundary data sourcing and geometry handling. |
| **A commercial map SDK as the default** | Possible, but introduces an API key, quota, and billing concerns; OSM-based keeps MVP dependency-light. Left as an option under the open tile-provider item. |

## Consequences

**Positive**
- Minimal backend: two numeric columns + a filtered, bounded list endpoint.
- No PostGIS to install, tune, back up, or explain.
- Filtering/lookup requirements met with ordinary indexed queries.

**Negative / trade-offs**
- The **tile-provider decision is deferred** and must not be forgotten — it is a
  gating item for the map UI and carries licensing/attribution obligations.
- No spatial queries (e.g. "assets within 500 m") in MVP; acceptable per D13.
- Offline map use is not supported (online-only MVP — AD-21).

**Follow-up**
- Track the tile-provider/licensing decision as an explicit open architecture item
  (PLAN.md §5 / TASKS.md); resolve before building the map screens.
