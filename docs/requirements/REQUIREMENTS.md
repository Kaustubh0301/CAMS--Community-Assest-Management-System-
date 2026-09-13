# CAMS — Requirements Specification

**Document:** `docs/requirements/REQUIREMENTS.md`
**Version:** 1.0 (BASELINE)
**Date:** 2026-09-04
**Status:** ✅ **BASELINED.** Requirements review pass complete. Decisions D1–D37
are approved and incorporated. A small number of items remain **OPEN / PENDING**
and are listed explicitly in §15.3 — these do **not** block the move to the
architecture / system-design phase, but must not be treated as decided.

> Supersedes v0.1 (DRAFT, 2026-09-04). See change log (Appendix A).

---

## 0. How to read this document

| Marker | Meaning |
|--------|---------|
| **APPROVED** | Backed by the project brief and/or an approved decision (D1–D37). This is the baseline. |
| **OPEN** | A decision is still required. No value here is authoritative. Listed in §15.3. |
| **PENDING (external)** | Blocked on information from outside the team (e.g. course faculty). |
| **DEFERRED** | Deliberately not specified now because the feature is Secondary/Future. |
| **Priority: MVP** | In the first working release. |
| **Priority: SECONDARY** | Planned after MVP is stable. |
| **Priority: FUTURE** | Optional / long-term; explicitly outside MVP and Secondary. |
| **Basis** | Traceability: `Dn` = approved decision n; `brief` = original project brief; `—` = elaboration with no separate decision. |

**Scope discipline:** this document does not choose frameworks, hosting, schemas, or
API shapes. Those belong to the architecture phase. Technology names that do appear
(Flutter, PostgreSQL, Android) are named **because an approved decision fixed them**
(D5, D26, D36) — not as architecture choices made here.

---

## 1. Decision register (D1–D37)

Approved on the requirements decision pass. Each row links to the requirements it
drives.

| # | Decision (summary) | Drives |
|---|--------------------|--------|
| D1 | Exactly four roles in MVP: Admin, Officer, Maintenance Worker, Citizen. No separate leadership role; Officers use reports/analytics for oversight. | §3, §7, FR-AUTH-002, §8.1 matrix |
| D2 | Citizens must have accounts in MVP; no anonymous complaints. Citizen can register, log in, submit complaints, view own complaints/status, receive in-app updates, rate closed complaints. | FR-AUTH-004, FR-COMP-*, FR-FEED-* |
| D3 | Every MVP complaint must be linked to a registered public asset. General-area complaints are Future/Secondary. | FR-COMP-001, §13 |
| D4 | MVP requires internet connectivity. Offline operation + sync are Future. | NFR-OFF-001, §14 |
| D5 | MVP is **one Flutter mobile application** used by all four roles. No web/desktop dashboard in MVP; a web dashboard is Future "if time permits". | §5.2, NFR-COMPAT-*, §14 |
| D6 | Officers manage the **entire local body** (all wards, assets, complaints, workers, maintenance, reports/analytics). Ward **filtering** is available; ward-specific **permissions** are Future. | §7, FR-AUTH-002, FR-COMP-007 |
| D7 | MVP complaint lifecycle: `PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → VERIFIED → CLOSED`. `REJECTED` is an alternative outcome during review. | FR-COMP-006, §8.6, §11 WF-3 |
| D8 | MVP asset **status**: `WORKING`, `BROKEN`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`. Kept separate from complaint status. | FR-ASSET-007 |
| D9 | MVP complaint **priority**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. Officer sets the final priority. AI priority suggestion is Future. | FR-MAINT-001, §14 |
| D10 | MVP notifications are **in-app only**. No SMS / WhatsApp / email. External channels are Future. | FR-NOTIF-*, §14 |
| D11 | Citizen complaint photo is **optional**; a complaint requires an asset + a description. A Worker **must** upload a repair/completion photo before a complaint can become `RESOLVED`. Multiple photos may be supported. | FR-COMP-002, FR-MAINT-005 |
| D12 | MVP languages: **English + Hindi**, user-selectable. Gujarati and other regional languages are Future. | NFR-I18N-*, FR-AUTH-011 |
| D13 | Asset registration captures latitude/longitude (map / current location) and stores a **Ward ID**. Official ward-boundary polygons are **not** required in MVP. | FR-ASSET-003, FR-GIS-003, FR-VILL-005 |
| D14 | Multiple Officers may exist; **no hierarchy**, all Officers have the same permissions. Escalation/hierarchy is Future. | §7, FR-AUTH-002 |
| D15 | MVP assigns one complaint to **one individual Worker**. No crew/team assignment. | FR-MAINT-002 |
| D16 | Citizen self-registration uses **mobile number + password**. No Aadhaar/government-ID. No anonymous complaints. OTP/SMS verification is Future. | FR-AUTH-004, §14 |
| D17 | **No comment/discussion thread** in MVP. Citizen can submit, view status, receive notifications, view resolution, and give a rating/feedback after closure. Comments are Secondary/Future. | FR-COMP-010, FR-FEED-* |
| D18 | Data retained for the **deployment lifetime** unless an authorized Admin action removes/deactivates it. Citizen name/mobile, complaint history, asset history, and photos are retained. Passwords are **never stored directly — secure hashing only**. Citizens cannot delete their own historical complaints. MVP includes a **simple privacy statement**. | NFR-SEC-002/006, FR-COMP-011, FR-AUTH-009/010 |
| D19 | System is designed for **multiple local bodies**; the evaluation uses one. Data structure supports `LocalBody → Village/Municipality → Ward → Assets → Complaints`. Do not hardcode a single village. | §5.2, FR-VILL-*, NFR-SCAL-001 |
| D20 | **No external integrations** in MVP (no Aadhaar, government portal, payment gateway, external GIS, SMS gateway, WhatsApp API, IoT). MVP is self-contained. | §6, §14 |
| D21 | Admin can reset passwords for Admin/Officer/Worker accounts. Citizen recovery is **basic and self-contained** (no external OTP/email infra) in MVP. Automated external recovery is Future. | FR-AUTH-006, §14 |
| D22 | Audit log for important actions (asset create/edit; complaint create/edit; status changes; worker assignment/reassignment; priority changes; verify/close/reject; important account/settings changes). Record actor, action, date/time, affected record, optional note. Do **not** log every UI click. | §8.10 FR-AUDIT-* |
| D23 | Asset categories are **Admin-configurable** without code changes. Initial set: Street Light, Hand Pump, Water Tank, Public Toilet, Road, Drain, School, Community Hall, Park, Bench, Bus Stop, Dustbin. | FR-ASSET-006 |
| D24 | Asset **condition** (`GOOD`, `FAIR`, `POOR`, `CRITICAL`) is separate from asset **status**. Status = operational state; condition = physical state. | FR-ASSET-008 |
| D25 | An asset can have **1–5 photos**. At least one is recommended, not mandatory. Images have size/compression controls. | FR-ASSET-004, NFR-PERF-003 |
| D26 | MVP is **Android-first**, target **Android 8.0+ / API 26+**. Flutter keeps iOS possible later. Server operations require connectivity. | NFR-COMPAT-*, D4 |
| D27 | UI must be rural/community friendly: large buttons, clear icons, minimal typing, English+Hindi, simple navigation, clear status labels, non-technical wording (e.g. "Start Repair"), automatic photo compression. | NFR-USE-* |
| D28 | Workers see **only their own** task information (assigned, completed, in progress, returned). Workers cannot see other workers' performance, government budget, village-wide analytics, or admin reports. | §7, §8.1 matrix, FR-ANLY-006 |
| D29 | Inventory is **Secondary**. Initially one central store; multiple stores are Future. | §8.11 FR-INV-* |
| D30 | Worker records **actual repair cost**; Officer reviews / confirms / records **official expenditure** and categorizes it. | FR-MAINT-005, FR-BUD-001 |
| D31 | Budget **allocation** is Secondary; supports allocation vs spending vs remaining (e.g. Ward 1 = ₹2,00,000; spent ₹1,25,000; remaining ₹75,000). | §8.12 FR-BUD-* |
| D32 | Worker performance uses **objective operational metrics**, not punitive scoring: tasks assigned, completed, average completion time, tasks returned, citizen rating. Show factual metrics, not subjective scores. | FR-ANLY-006, FR-FEED-005 |
| D33 | MVP report formats: **PDF + Excel**. MVP reports: (1) Complaint report, (2) Asset register report. Advanced reports later. | §8.14 FR-RPT-* |
| D34 | SLA is **reporting-only** initially (target vs actual vs within/over). No automatic escalation in MVP; escalation is Future. | §8.15 FR-SLA-* |
| D35 | QR is **Future**. When built: role-dependent access (Citizen → details + report; Worker → asset + maintenance; Officer → full asset/history). No anonymous QR access. | §8.16 FR-QR-* |
| D36 | **Daily PostgreSQL backup**, ≥ **7 days** retention. Uploaded photos/media also backed up. Restore tested before final evaluation. Documented/manual backup & restore is acceptable for the student MVP initially. | NFR-BAK-* |
| D37 | **Do not invent university dates.** DSN3099 schedule/rubric/deadlines come from faculty/course material. Keep OPEN/PENDING until supplied. | §15.3 OQ-36 |

---

## 2. Purpose

CAMS (Community Asset Management System) provides a single shared system of record
for public community assets and their maintenance, so that a local government body
(Gram Panchayat / municipality) and the citizens it serves can register assets,
report problems, coordinate repairs, and see the history and cost of maintenance
work.

This document defines **what** CAMS must do and the qualities it must have.

---

## 3. Problem statement

Public infrastructure in villages and small municipalities — street lights, hand
pumps, water tanks, public toilets, roads, drains, schools, community halls, parks,
benches, bus stops, dustbins, and similar — is often tracked with paper records,
informal communication, or fragmented records. This leads to poor asset records,
delayed maintenance, duplicate complaints, inefficient maintenance spending, and
limited visibility into asset history.

CAMS centralizes asset data and the complaint-to-repair workflow to reduce these
problems.

---

## 4. Target users / stakeholders

| Stakeholder | Role in CAMS | Basis |
|-------------|--------------|-------|
| **Admin** | System configuration and user administration. | D1 |
| **Government / Panchayat Officer** | Authoritative management of assets and the complaint workflow for the whole local body; oversight via reports/analytics. | D1, D6 |
| **Maintenance Worker** | Executes assigned repair work and records it; sees only their own work. | D1, D15, D28 |
| **Citizen** | Registers, reports asset issues, tracks their own complaints, rates resolved work. | D2, D16 |
| Panchayat / municipal leadership | **Not a login role.** Served by reports/analytics that Officers produce. | D1 |
| University evaluators (DSN3099 faculty) | Assess the delivered project. Not a system user. Provide course schedule/rubric (see OQ-36). | D37 |

---

## 5. Project goals

- **G-1** Maintain an accurate, centralized register of public community assets,
  each with location, category, status, condition, and photos.
- **G-2** Let registered citizens report asset problems from a mobile device and
  follow those reports to resolution.
- **G-3** Give officers a queue to triage, prioritize, assign, verify, and close
  complaints across the whole local body.
- **G-4** Give maintenance workers a simple view of their own assigned work and a
  structured way to record completion evidence.
- **G-5** Build a per-asset maintenance history automatically from closed complaints.
- **G-6** Provide oversight via analytics and PDF/Excel reports.
- **G-7** (Secondary) Track maintenance expenditure and inventory consumption.
- **G-8** Be usable on mid-range Android phones (API 26+) with intermittent rural
  connectivity, in English and Hindi, with a low-literacy-friendly UI.

---

## 6. Scope

### 6.1 In scope for MVP

Authentication and role-based authorization for four roles; Admin setup of the
`LocalBody → Village/Municipality → Ward` structure; asset registration and
lifecycle with photos and map location; interactive map with asset selection;
registered-citizen complaints against an asset (description required, photo
optional); officer review, prioritization, and assignment to one worker; worker
execution recording with a mandatory completion photo and repair cost; officer
verification and close; automatic per-asset maintenance history; citizen
feedback/rating on closed complaints; in-app notifications; audit log of important
actions; basic analytics; two reports (complaint, asset register) exported as
PDF/Excel; a simple privacy statement; documented backup & restore.

### 6.2 In scope later (Secondary / Future)

Inventory (Secondary), budget/expenditure and allocation (Secondary), advanced
analytics and reports (Secondary), SLA target reporting (Secondary), citizen
comment threads (Secondary), general-area complaints (Secondary/Future), complaint
reopening (Future/Secondary — see OQ-25), QR identification (Future), offline
operation (Future), a web/desktop dashboard (Future), external notification
channels (Future), officer hierarchy/escalation (Future), AI features (Future).

### 6.3 Deployment model (D19)

- The data model must support **multiple local bodies**:
  `LocalBody → Village/Municipality → Ward → Asset → Complaint`.
- The demonstration/evaluation runs on **one selected local body**.
- The system must **not** be hardcoded to a single village.
- *(Modeling note: the semantic split between "LocalBody" and
  "Village/Municipality" as two distinct levels vs one is flagged as **OQ-39** for
  the data-modeling step; D19's hierarchy string is taken as given.)*

---

## 7. Out-of-scope items

Not part of CAMS as approved (some may become Future work; none may be assumed):

- Any **external integration** (D20): Aadhaar / government-ID, government portals,
  payment gateways, external GIS/land-records databases, SMS gateways, WhatsApp
  API, IoT/sensor feeds.
- Any **AI/ML feature** as a committed deliverable (see §14).
- Financial disbursement, payroll, procurement/purchase-order processing, tendering,
  or any actual money movement.
- Legal/statutory/policy-compliance features. No government policy or regulation is
  assumed by this document.
- A **web or desktop dashboard** in MVP (D5) — Future only.
- **Offline** use / background sync in MVP (D4) — Future only.
- **Anonymous** complaints or QR access (D16, D35).
- Citizen self-deletion of historical complaints (D18).
- Real-time telemetry, public open-data portal, multi-tenant billing/SaaS.
- Grievances unrelated to a **registered asset** in MVP (D3).

---

## 8. User roles and responsibilities

| Role | Responsibilities | Notes / Basis |
|------|------------------|---------------|
| **Admin** | Create and maintain the `LocalBody / Village-Municipality / Ward` structure; create and manage Officer/Worker accounts; reset Admin/Officer/Worker passwords; configure asset categories; view audit log; oversee backup/restore (operational). | D1, D19, D21, D23, D22, D36 |
| **Officer** | Register/edit/decommission assets; review incoming complaints (accept → assign, or REJECT with reason); set final priority; assign to one Worker; verify completed work; close complaints; (Secondary) record/categorize official expenditure, manage inventory, configure SLA targets; view all analytics and generate reports for the whole local body; use ward filtering. | D1, D6, D9, D14, D15, D30 |
| **Maintenance Worker** | View **only own** assigned tasks; start work (`IN_PROGRESS`); record completion — mandatory repair/completion photo, repair cost, remarks — to move a complaint to `RESOLVED`; (Secondary) record materials consumed. See only own task metrics. | D11, D15, D28, D30 |
| **Citizen** | Self-register (mobile number + password); log in; browse assets/map; submit a complaint against a registered asset (description required, photo optional); view own complaints and status; receive in-app notifications; view the resolution; give a rating (and optional one-time feedback text) after `CLOSED`. No comment thread; cannot delete history. | D2, D3, D11, D16, D17, D18 |

**Multiple Officers:** allowed, all equal, no hierarchy (D14). **Ward-scoped
Officer permissions:** Future (D6).

### 8.1 Role-capability matrix (MVP)

✔ = allowed in MVP · `S` = Secondary · `F` = Future · blank = not allowed · "own" =
only records the user created / is assigned.

| Capability | Admin | Officer | Worker | Citizen | Basis |
|------------|:-----:|:------:|:-----:|:------:|-------|
| Manage LocalBody / Village-Municipality / Ward | ✔ | | | | D19 |
| Manage user accounts & roles | ✔ | | | | D2 |
| Reset passwords (Admin/Officer/Worker) | ✔ | | | | D21 |
| Recover own password (self-service, basic) | ✔ | ✔ | ✔ | ✔ | D21 |
| Configure asset categories | ✔ | | | | D23 |
| View audit log | ✔ | (S) | | | D22 |
| Register / edit / decommission assets | ✔ | ✔ | | | brief, D6 |
| View assets & map | ✔ | ✔ | ✔ | ✔ | brief |
| Ward filtering of assets/complaints | ✔ | ✔ | own | | D6 |
| Create complaint (against a registered asset) | S | S | S | ✔ | D3, D17 (proactive = S) |
| View full complaint queue (whole local body) | ✔ | ✔ | | | D6 |
| View own / assigned complaints | ✔ | ✔ | ✔ (assigned) | ✔ (own) | D2, D28 |
| Accept & assign; REJECT during review | ✔ | ✔ | | | D7 |
| Set / change final priority | ✔ | ✔ | | | D9 |
| Assign / reassign to one Worker | ✔ | ✔ | | | D15 |
| Start work; record completion (photo+cost) | | | ✔ (assigned) | | D11, D30 |
| Verify & close | ✔ | ✔ | | | D7 |
| Rate + optional feedback after CLOSED | | | | ✔ (own) | D2, D17 |
| Record / categorize official expenditure | S | S | | | D30 |
| Manage inventory | S | S | consume (S) | | D29 |
| Configure SLA targets | S | S | | | D34 |
| View analytics & reports | ✔ | ✔ | own metrics only | | D6, D28 |
| Select preferred language (English/Hindi) | ✔ | ✔ | ✔ | ✔ | D12 |

---

## 9. Functional requirements

Every row below is **APPROVED** unless its Notes say **OPEN** / **DEFERRED**.

### 9.1 Authentication and authorization (FR-AUTH)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-AUTH-001 | Authentication is required before any role-specific function is used. | MVP | brief |
| FR-AUTH-002 | The system enforces exactly four roles — Admin, Officer, Maintenance Worker, Citizen — per the §8.1 matrix. No leadership role. | MVP | D1 |
| FR-AUTH-003 | An Admin can create, edit, deactivate, and reactivate Officer and Maintenance Worker accounts. | MVP | D1, D2 |
| FR-AUTH-004 | A Citizen can self-register with a **mobile number + password** and log in. No Aadhaar/government-ID; no OTP/SMS verification in MVP. | MVP | D2, D16 |
| FR-AUTH-005 | A signed-in user can sign out, ending the session on that device. | MVP | brief |
| FR-AUTH-006 | An Admin can reset passwords for Admin/Officer/Worker accounts. Citizen password recovery in MVP is **basic and self-contained** — no external OTP/email/SMS. The concrete citizen-recovery mechanism within this constraint is a design item (**OQ-40**). | MVP | D21 |
| FR-AUTH-007 | *(Superseded — audit trail is now specified in §9.10, FR-AUDIT.)* | — | D22 |
| FR-AUTH-008 | Anonymous or lightly-identified complaint submission. | FUTURE | D16 |
| FR-AUTH-009 | The app displays a **simple privacy statement**: what citizen data is collected (name, mobile, complaint text, photos, location), that it is retained for the deployment lifetime, and that citizens cannot delete their own historical complaints. | MVP | D18 |
| FR-AUTH-010 | An Admin can deactivate a user account; hard deletion of a user/personal data is an explicit, **audited** Admin action. By default all records are retained (NFR-SEC-006). | MVP | D18 |
| FR-AUTH-011 | Any user can select a preferred UI language (English or Hindi); the choice persists for that user. | MVP | D12 |
| FR-AUTH-012 | Passwords are validated against a basic strength rule on set/reset. (Rule details = design; no external service.) | MVP | D16, D18 |

Passwords are stored only as secure hashes — see NFR-SEC-002.

### 9.2 Local body / village / ward management (FR-VILL)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-VILL-001 | An Admin can create a **LocalBody** record (name + identifier). | MVP | D19 |
| FR-VILL-002 | An Admin can create **Village / Municipality** records under a LocalBody, and **Ward** records under a Village/Municipality (name/number). | MVP | D19 |
| FR-VILL-003 | An Admin can edit and deactivate LocalBody / Village-Municipality / Ward records; deactivation never deletes historical data. | MVP | D18, D19 |
| FR-VILL-004 | Every asset is associated with exactly one Ward (and therefore one Village/Municipality and one LocalBody). | MVP | D13, D19 |
| FR-VILL-005 | Wards store an **identifier only** in MVP. Official ward-boundary polygons are **not** required; an optional ward centroid may be stored for map centring. | MVP (no polygons) / SECONDARY (centroid, overlays) | D13 |
| FR-VILL-006 | The system must not assume a single Village/LocalBody anywhere in data or logic. | MVP | D19 |

### 9.3 Asset management (FR-ASSET)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-ASSET-001 | An Officer or Admin can register a public asset. | MVP | brief, D6 |
| FR-ASSET-002 | Each asset gets a unique, system-assigned identifier, stable for its lifetime. | MVP | brief |
| FR-ASSET-003 | Each asset records: category, Ward ID, **latitude/longitude** (from map or current location), operational **status**, physical **condition**, and installation date. | MVP | D13, D8, D24 |
| FR-ASSET-004 | Each asset supports **1–5 photos**. At least one is recommended, not mandatory. | MVP | D25 |
| FR-ASSET-005 | Each asset optionally records warranty information (provider, reference, expiry date). Storing it is MVP; warranty-expiry **alerts** are Secondary (FR-NOTIF-004). | MVP (store) | brief |
| FR-ASSET-006 | Asset **categories are Admin-configurable without code changes**. Seed list: Street Light, Hand Pump, Water Tank, Public Toilet, Road, Drain, School, Community Hall, Park, Bench, Bus Stop, Dustbin. | MVP | D23 |
| FR-ASSET-007 | Asset **status** is one of `WORKING`, `BROKEN`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`. Status is the operational state and is **distinct** from complaint status. | MVP | D8 |
| FR-ASSET-008 | Asset **condition** is one of `GOOD`, `FAIR`, `POOR`, `CRITICAL`. Condition is the physical state and is **distinct** from status. | MVP | D24 |
| FR-ASSET-009 | An Officer can edit an asset's attributes and can set status `DECOMMISSIONED` without deleting its history. | MVP | D8, D18 |
| FR-ASSET-010 | Users can list and filter assets by ward, category, status, and condition, and open a single asset detail view. | MVP | brief |
| FR-ASSET-011 | The asset detail view shows the asset's maintenance history (§9.7) and its open complaints. | MVP | brief |
| FR-ASSET-012 | Hard deletion of an asset is restricted to Admin and is blocked (or explicitly confirmed + audited) when linked history exists. | SECONDARY | D18, D22 |

### 9.4 GIS / map (FR-GIS)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-GIS-001 | Registered assets are shown as markers on an interactive map. | MVP | brief |
| FR-GIS-002 | Selecting a marker opens that asset's detail view; from there a Citizen can start a complaint. | MVP | brief, D3 |
| FR-GIS-003 | When registering/editing an asset, the user can set its location from the device's current position or by placing/adjusting a point on the map (captured as latitude/longitude). | MVP | D13 |
| FR-GIS-004 | The map can filter displayed assets by category and status, and by ward. | MVP | D6 |
| FR-GIS-005 | The map visually distinguishes assets that have open complaints or are `BROKEN` / `DECOMMISSIONED`. | SECONDARY | — |
| FR-GIS-006 | Ward-boundary overlays on the map. | SECONDARY | D13 (needs centroid/boundary data) |
| FR-GIS-007 | Map use with no network connection. | FUTURE | D4 |

### 9.5 Citizen complaints (FR-COMP)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-COMP-001 | A registered Citizen can create a complaint **about a specific registered asset**, chosen from a list or a map marker. A complaint with no linked asset is not allowed in MVP. | MVP | D2, D3 |
| FR-COMP-002 | A complaint **requires**: the linked asset and a free-text description. A citizen photo is **optional**. Multiple photos may be supported. | MVP | D11 |
| FR-COMP-003 | A complaint captures the linked asset's location and the submission timestamp. | MVP | brief |
| FR-COMP-004 | Each complaint gets a unique identifier and initial status `PENDING`. | MVP | D7 |
| FR-COMP-005 | A Citizen can view a list of **their own** complaints and each one's current status and (when available) resolution details. | MVP | D2, D17 |
| FR-COMP-006 | The complaint lifecycle is exactly: `PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → VERIFIED → CLOSED`, with `REJECTED` as an alternative terminal outcome **from `PENDING` during Officer review**. Allowed transitions are defined in §11 WF-3. | MVP | D7 |
| FR-COMP-007 | An Officer can view all complaints for the whole local body in a queue, filterable by status, ward, category, and priority. | MVP | D6 |
| FR-COMP-008 | During review an Officer can set a complaint to `REJECTED` with a reason; the Citizen sees the outcome and reason. | MVP | D7 |
| FR-COMP-009 | Duplicate-complaint linking (manual). | SECONDARY | D17 |
| FR-COMP-010 | Comment / discussion thread on a complaint. | SECONDARY | D17 |
| FR-COMP-011 | A Citizen **cannot delete** their own historical complaints. | MVP | D18 |
| FR-COMP-012 | Complaint **reopening** by a Citizen after `CLOSED`. Classified **Future/Secondary**; **no detailed reopen policy is approved** (window, who, how many times) — see **OQ-25**. | FUTURE / SECONDARY (undefined) | D-none; flagged |
| FR-COMP-013 | Proactive complaint creation by an Officer or Worker (not from a Citizen). | SECONDARY | D17 |
| FR-COMP-014 | General-area complaints (not tied to a registered asset). | SECONDARY / FUTURE | D3 |

### 9.6 Maintenance workflow (FR-MAINT)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-MAINT-001 | An Officer sets/changes a complaint's **final priority**, one of `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. | MVP | D9 |
| FR-MAINT-002 | An Officer assigns a complaint to **one individual Maintenance Worker** (moving it `PENDING → ASSIGNED`). Reassignment to another single Worker is possible; assignment history is retained. No crew/team assignment. | MVP | D15 |
| FR-MAINT-003 | A Worker sees a list of complaints **assigned to them only**, with priority and asset location. | MVP | D28 |
| FR-MAINT-004 | A Worker can start an assigned task, moving it `ASSIGNED → IN_PROGRESS` (UI label e.g. "Start Repair"). | MVP | D7, D27 |
| FR-MAINT-005 | To move a complaint `IN_PROGRESS → RESOLVED`, the Worker must record: **at least one repair/completion photo (mandatory)**, the **actual repair cost**, and remarks. A "before" photo is optional/recommended. Multiple photos may be supported. | MVP | D11, D30 |
| FR-MAINT-006 | On completion the Worker can record materials/quantities used (free text/quantity in MVP; deducts inventory when the inventory module is enabled). | SECONDARY | D29, D30 |
| FR-MAINT-007 | When a complaint reaches `RESOLVED`, the assigning Officer is notified (in-app). | MVP | D10 |
| FR-MAINT-008 | An Officer reviews a `RESOLVED` complaint and either (a) marks it `VERIFIED` and then `CLOSED` (these may be one UI action; both transitions are logged), or (b) **returns** it for rework. On return the complaint goes back to `IN_PROGRESS` with a reason and is counted as a "returned" task. *(Whether "returned" is a flag on `IN_PROGRESS` or a distinct state is **OQ-38**.)* | MVP | D7, D32 |
| FR-MAINT-009 | When closing, the Officer may update the linked asset's **status** and **condition**; these are separate fields (FR-ASSET-007/008). | MVP | D8, D24 |
| FR-MAINT-010 | Every complaint state transition is recorded with actor, timestamp, and any note (feeds the audit log, §9.10). | MVP | D22 |
| FR-MAINT-011 | Officer records/confirms **official expenditure** for a complaint, categorizing the Worker-entered cost (e.g. labor / material / transport / other). | SECONDARY | D30 |
| FR-MAINT-012 | Automatic escalation of overdue complaints. | FUTURE | D34, D14 |

### 9.7 Asset maintenance history (FR-HIST)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-HIST-001 | The system automatically creates a maintenance-history entry for an asset when a related complaint reaches `CLOSED`. | MVP | D7, brief |
| FR-HIST-002 | Each history entry records: date, originating complaint reference, work summary/remarks, Worker, repair cost, completion photo reference(s), and the asset status/condition set at closing. | MVP | D11, D30 |
| FR-HIST-003 | The asset detail view shows maintenance history in reverse chronological order. | MVP | brief |
| FR-HIST-004 | An Officer can add a manual maintenance-history entry not tied to a citizen complaint (routine servicing). | SECONDARY | — |

### 9.8 Citizen feedback & rating (FR-FEED)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-FEED-001 | After a complaint reaches `CLOSED`, the originating Citizen can submit **one rating** for the resolution. Rating is optional (the Citizen may skip it). `REJECTED` complaints are not rated. | MVP | D2, D17 |
| FR-FEED-002 | The rating uses a single fixed simple scale. **The scale (e.g. 1–5) is OPEN — see OQ-37.** | MVP (capability) / OPEN (scale) | D-none; flagged |
| FR-FEED-003 | With the rating, the Citizen may optionally add a **one-time short free-text feedback note**. This is not a discussion thread and cannot be added to or replied to (consistent with D17). | MVP | D17 |
| FR-FEED-004 | A rating/feedback, once submitted, is final for MVP (not editable). One rating per complaint. | MVP | — |
| FR-FEED-005 | The rating value and any feedback note are visible on the complaint record to the Officer and Admin, and contribute to Worker performance metrics (FR-ANLY-006). The assigned Worker can see the citizen rating for **their own** completed complaints only. | MVP | D28, D32 |
| FR-FEED-006 | Aggregate/average citizen rating is shown in analytics (Officer/Admin) and in the Worker's own metrics. | SECONDARY | D32 |

### 9.9 Notifications (FR-NOTIF)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-NOTIF-001 | The system sends **in-app** notifications to a Citizen on every status change of their complaint: accepted/assigned, in progress, resolved, verified, closed, rejected. | MVP | D10 |
| FR-NOTIF-002 | The system sends an in-app notification to a Worker when a complaint is assigned to, or reassigned away from, them, and when their `RESOLVED` work is returned for rework. | MVP | D10 |
| FR-NOTIF-003 | The system sends an in-app notification to the assigning Officer when a Worker marks a complaint `RESOLVED`. | MVP | D10 |
| FR-NOTIF-004 | Warranty-expiry notification to an Officer (depends on FR-ASSET-005). | SECONDARY | brief |
| FR-NOTIF-005 | SMS / WhatsApp / email / push notification channels. | FUTURE | D10, D20 |

### 9.10 Audit trail (FR-AUDIT)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-AUDIT-001 | The system records an audit entry for these actions: asset create/edit; complaint create/edit; complaint status changes; worker assignment/reassignment; priority changes; verify / close / reject; important account and settings changes (create/deactivate/delete account, role change, category change, password reset). | MVP | D22 |
| FR-AUDIT-002 | Each audit entry records: **actor**, **action**, **date/time**, **affected record** (type + id), and an **optional note**. | MVP | D22 |
| FR-AUDIT-003 | The system does **not** log routine UI navigation / button clicks that are not one of the actions in FR-AUDIT-001. | MVP | D22 |
| FR-AUDIT-004 | An Admin can view and filter the audit log (by actor, action type, affected record, date range). | MVP | D22 |
| FR-AUDIT-005 | Officer read access to a relevant subset of the audit log. | SECONDARY | D22 |

### 9.11 Inventory (FR-INV) — Secondary

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-INV-001 | An Officer/Admin can define inventory items (e.g. bulbs, pipes, valves, paint, cement, benches) with a unit of measure. | SECONDARY | D29 |
| FR-INV-002 | The system tracks current stock quantity per item in **one central store** (multiple stores are Future). | SECONDARY | D29 |
| FR-INV-003 | An Officer/Admin can record stock additions and adjustments with a reason. | SECONDARY | D29 |
| FR-INV-004 | A Worker recording completion (FR-MAINT-006) can record items/quantities consumed, decrementing stock. | SECONDARY | D29, D30 |
| FR-INV-005 | The system flags items at or below a configurable low-stock threshold. Procurement/purchasing stays out of scope. | SECONDARY | D29 |

### 9.12 Budget & expenditure (FR-BUD) — Secondary

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-BUD-001 | An Officer/Admin records official maintenance **expenditure** entries categorized as labor, material, transport, or other, based on / reconciled with the Worker-entered repair cost. | SECONDARY | D30 |
| FR-BUD-002 | Expenditure entries link to a complaint / maintenance-history entry and thereby to an asset and ward. | SECONDARY | D30 |
| FR-BUD-003 | An Admin/Officer can record a **budget allocation** (amount, period, scope such as ward or category); the system shows allocation vs spent vs remaining (e.g. ₹2,00,000 / ₹1,25,000 / ₹75,000). | SECONDARY | D31 |
| FR-BUD-004 | The system shows spend totals by period, ward, asset category, and expenditure type. | SECONDARY | D31, D32 |

### 9.13 Analytics (FR-ANLY)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-ANLY-001 | Show counts of assets by **status** and by **condition**, filterable by ward and category. | MVP | D8, D24 |
| FR-ANLY-002 | Show complaint counts by status, ward, category, and priority over a selectable period. | MVP | D6, D9 |
| FR-ANLY-003 | Show complaint **resolution time** (submission → `CLOSED`): average/median and distribution. | SECONDARY | — |
| FR-ANLY-004 | Show repair cost / expenditure totals and averages by ward, category, and period. | SECONDARY | D30, D31 |
| FR-ANLY-005 | Show ward-wise distribution of assets and complaints. | SECONDARY | — |
| FR-ANLY-006 | Show **Worker performance as objective operational metrics only**: tasks assigned, tasks completed, average completion time, tasks returned, average citizen rating. No subjective/punitive score. Officers/Admin see all Workers; a Worker sees only their own metrics. | SECONDARY | D28, D32 |
| FR-ANLY-007 | Show trends over time (complaints per month, cost per month). | SECONDARY | — |

### 9.14 Reports (FR-RPT)

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-RPT-001 | Generate a **Complaint report** for a selected period and filters (status, ward, category, priority). | MVP | D33 |
| FR-RPT-002 | Generate an **Asset register report** (assets with key attributes) for selected filters. | MVP | D33 |
| FR-RPT-003 | MVP reports are exportable as **PDF** and **Excel**. | MVP | D33 |
| FR-RPT-004 | Monthly summary report. | SECONDARY | D33 |
| FR-RPT-005 | Ward report. | SECONDARY | D33 |
| FR-RPT-006 | Maintenance-history report (per asset or set). | SECONDARY | D33 |
| FR-RPT-007 | Budget / expenditure report. | SECONDARY | D31, D33 |

### 9.15 SLA tracking (FR-SLA) — Secondary

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-SLA-001 | Configurable **target resolution time per priority**. Target values are **not yet approved** (the "HIGH = 48h" figure in D34 is illustrative) — see OQ-41. | SECONDARY | D34 |
| FR-SLA-002 | Reports/analytics show target vs actual and a within-target / over-target result per complaint and in aggregate. | SECONDARY | D34 |
| FR-SLA-003 | **No automatic escalation** in MVP or in this Secondary scope; escalation is Future (FR-MAINT-012). | (constraint) | D34 |

### 9.16 QR asset identification (FR-QR) — Future

| ID | Requirement | Priority | Basis |
|----|-------------|----------|-------|
| FR-QR-001 | Associate a QR code value with an asset. | FUTURE | D35 |
| FR-QR-002 | An authenticated user scans a QR code in the app to open the asset. Role-dependent result: Citizen → details + report issue; Worker → asset + maintenance; Officer → full asset/history. **No anonymous access.** | FUTURE | D35 |
| FR-QR-003 | Produce a printable QR label for an asset. | FUTURE | D35 |

---

## 10. Non-functional requirements

### 10.1 Security (NFR-SEC)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-SEC-001 | All access is gated by authentication and by the §8.1 role-capability rules. | D1, D6, D28 |
| NFR-SEC-002 | Passwords are **never stored directly**; only a secure salted hash is stored. | D18 |
| NFR-SEC-003 | Data in transit between the app and the backend is encrypted. | — |
| NFR-SEC-004 | Personal data collected about citizens is limited to name, mobile number, complaint content, photos, and location. | D18 |
| NFR-SEC-005 | Security-relevant actions are attributable to a user and time (supports FR-AUDIT). | D22 |
| NFR-SEC-006 | **Data retention:** all records (citizen name/mobile, complaint history, asset history, photos) are retained for the deployment lifetime unless an authorized, audited Admin action removes them. Citizens cannot delete their own history. A simple privacy statement is shown in the app (FR-AUTH-009). | D18 |
| NFR-SEC-007 | No external identity/verification services are used in MVP (no Aadhaar, OTP gateway, email). | D16, D20, D21 |

### 10.2 Performance (NFR-PERF)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-PERF-001 | Common read screens (asset list, complaint list, complaint detail, worker task list) load quickly on a mid-range Android phone on typical rural mobile data. **Numeric target: OPEN (OQ-14 / OQ-17).** | D26 |
| NFR-PERF-002 | The map stays usable for the asset volume of one local body; large counts degrade gracefully (clustering/pagination). Threshold depends on OQ-14. | D19 |
| NFR-PERF-003 | Images are automatically compressed/resized on capture before upload, with size controls; uploads tolerate slow/unstable connections (retry) without data loss. | D25, D27 |
| NFR-PERF-004 | PDF/Excel report generation for a normal period/scope completes within a reasonable time (target set during design). | D33 |

### 10.3 Reliability (NFR-REL)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-REL-001 | No confirmed user action (submitted complaint, recorded completion, verify/close) is silently lost; on failure the user is informed and can retry. | — |
| NFR-REL-002 | Concurrent edits to the same complaint/asset by different users do not corrupt data; conflict handling defined in design. | D14 (multiple equal Officers) |
| NFR-REL-003 | **MVP availability target is OPEN (OQ-17).** No percentage/SLA is approved; none is to be invented. | flagged |

### 10.4 Usability (NFR-USE)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-USE-001 | A first-time registered Citizen can submit a valid complaint without training. | D27 |
| NFR-USE-002 | Citizen and Worker flows are operable by users with low literacy and limited smartphone experience: large buttons, clear icons, minimal typing, simple navigation. | D27 |
| NFR-USE-003 | Wording is non-technical and action-oriented (e.g. "Start Repair", not "Set status IN_PROGRESS"); status labels are clear and plain. | D27 |
| NFR-USE-004 | Primary actions for each role are reachable in a few taps from the home screen. | D27 |
| NFR-USE-005 | Error messages are specific and actionable, not raw technical output. | D27 |

### 10.5 Internationalization (NFR-I18N)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-I18N-001 | The UI is fully available in **English and Hindi**; the user selects their preferred language (FR-AUTH-011). | D12 |
| NFR-I18N-002 | All user-facing strings are externalized so further languages (e.g. Gujarati) can be added later without code changes. | D12 |

### 10.6 Maintainability (NFR-MAINT)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-MAINT-001 | Code and tests are the source of truth; every MVP functional requirement has automated test coverage sufficient to demonstrate it (see `docs/testing/`). | CLAUDE.md |
| NFR-MAINT-002 | Reference data that a decision marks configurable — asset categories (D23), and later SLA targets, priorities if extended — is changeable without code changes. | D23, D34 |
| NFR-MAINT-003 | Secondary/Future modules (inventory, budget, advanced analytics/reports, SLA, QR, web dashboard, offline) can be added without redesigning MVP data. | D29, D31, D35, D5, D4 |
| NFR-MAINT-004 | Project state is tracked in repository files (`PLAN.md`, `TASKS.md`, `docs/`, `memory/`), not only in conversation. | CLAUDE.md |

### 10.7 Scalability (NFR-SCAL)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-SCAL-001 | The data model supports **multiple local bodies** (`LocalBody → Village/Municipality → Ward → Asset → Complaint`) with no schema redesign; nothing is hardcoded to one village. | D19 |
| NFR-SCAL-002 | **Expected data volumes are OPEN (OQ-14)** — number of assets, users per role, complaints/month, photo/media volume. No capacity figure is approved. | flagged |
| NFR-SCAL-003 | Media (photo) storage growth is planned for; per-asset (1–5) and per-complaint photo limits plus compression bound growth. | D25 |

### 10.8 Data backup / recovery (NFR-BAK)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-BAK-001 | The PostgreSQL database is backed up **daily**, with **at least 7 days** retention. | D36 |
| NFR-BAK-002 | Uploaded photos/media are included in the backup. | D36 |
| NFR-BAK-003 | A restore procedure is documented and **tested before final evaluation**. | D36 |
| NFR-BAK-004 | For the student MVP, a **documented manual** backup & restore procedure is acceptable initially (automation can come later). | D36 |

### 10.9 Compatibility / platform (NFR-COMPAT)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-COMPAT-001 | MVP is a **single Flutter mobile application** used by all four roles. No separate web/desktop client in MVP. | D5 |
| NFR-COMPAT-002 | MVP is **Android-first**, targeting **Android 8.0 / API 26 and above**. | D26 |
| NFR-COMPAT-003 | The codebase stays structured so an iOS build is possible later; iOS is not an MVP deliverable. | D26 |
| NFR-COMPAT-004 | The app requires connectivity for all server operations; intermittent mobile data is expected and handled gracefully (retry, clear offline error states) but true offline use is Future. | D4, D26 |

### 10.10 Offline capability (NFR-OFF)

| ID | Requirement | Basis |
|----|-------------|-------|
| NFR-OFF-001 | Offline operation and background synchronization are **FUTURE**, not MVP. MVP requires internet connectivity. | D4 |

---

## 11. User stories per role

Format: *As a &lt;role&gt;, I want &lt;goal&gt;, so that &lt;benefit&gt;.* Each maps to
requirements.

### 11.1 Admin
- **US-ADM-01** Create the LocalBody, its Villages/Municipalities and Wards, so the
  system reflects the real area. *(FR-VILL-001..003)*
- **US-ADM-02** Create Officer and Worker accounts and reset their passwords, so
  only authorized staff use the system. *(FR-AUTH-003, FR-AUTH-006)*
- **US-ADM-03** Configure the asset category list without code changes, so it
  matches local asset types. *(FR-ASSET-006)*
- **US-ADM-04** Deactivate a departed staff member without deleting their past
  work. *(FR-AUTH-003, FR-AUTH-010, NFR-SEC-006)*
- **US-ADM-05** View and filter the audit log, so important changes are traceable.
  *(FR-AUDIT-004)*
- **US-ADM-06** Know that a daily backup exists and restore has been tested.
  *(NFR-BAK-001..004)*

### 11.2 Government / Panchayat Officer
- **US-OFF-01** Register an asset with category, ward, map location, status,
  condition, installation date, and up to 5 photos. *(FR-ASSET-001..004, FR-GIS-003)*
- **US-OFF-02** See all assets on a map and in a filtered list for the whole local
  body, with a ward filter. *(FR-ASSET-010, FR-GIS-001/004)*
- **US-OFF-03** Work a single queue of all complaints, filter by status/ward/
  category/priority. *(FR-COMP-007)*
- **US-OFF-04** Reject an invalid complaint with a reason during review.
  *(FR-COMP-008)*
- **US-OFF-05** Set the final priority and assign the complaint to one Worker.
  *(FR-MAINT-001/002)*
- **US-OFF-06** Be notified when a Worker marks work `RESOLVED`, review the
  completion photo and cost, then verify & close — or return it for rework.
  *(FR-NOTIF-003, FR-MAINT-008)*
- **US-OFF-07** Have the asset's maintenance history updated automatically on close.
  *(FR-HIST-001/002)*
- **US-OFF-08** See asset status/condition counts and complaint counts, and
  generate the Complaint and Asset-register reports as PDF/Excel. *(FR-ANLY-001/002,
  FR-RPT-001..003)*
- **US-OFF-09 (Secondary)** Record and categorize official expenditure and track a
  ward budget allocation vs spend. *(FR-MAINT-011, FR-BUD-001..004)*

### 11.3 Maintenance Worker
- **US-WRK-01** See only the complaints assigned to me, with priority and asset
  location. *(FR-MAINT-003, FR-ANLY-006)*
- **US-WRK-02** Tap "Start Repair" to mark a task in progress. *(FR-MAINT-004,
  NFR-USE-003)*
- **US-WRK-03** Upload a completion photo and enter the repair cost and remarks to
  mark the task `RESOLVED`. *(FR-MAINT-005)*
- **US-WRK-04** Be notified when a task is assigned to me or returned for rework.
  *(FR-NOTIF-002)*
- **US-WRK-05** See my own metrics (assigned, completed, avg time, returned, my
  citizen ratings) — and nothing about other workers or budgets. *(FR-ANLY-006,
  FR-FEED-005, D28)*
- **US-WRK-06 (Secondary)** Record materials/quantities used. *(FR-MAINT-006,
  FR-INV-004)*

### 11.4 Citizen
- **US-CIT-01** Register with my mobile number and a password, and log in.
  *(FR-AUTH-004)*
- **US-CIT-02** Pick a public asset from a list or a map marker and file a complaint
  with a description (photo optional). *(FR-COMP-001/002, FR-GIS-002)*
- **US-CIT-03** See the status of each complaint I filed and its resolution.
  *(FR-COMP-005)*
- **US-CIT-04** Get in-app notifications as my complaint moves through its stages.
  *(FR-NOTIF-001)*
- **US-CIT-05** Rate the resolution after my complaint is `CLOSED`, and optionally
  leave a one-time feedback note. *(FR-FEED-001..004)*
- **US-CIT-06** Use the app in Hindi or English. *(FR-AUTH-011, NFR-I18N-001)*
- **US-CIT-07** Read a simple privacy statement about what data is kept.
  *(FR-AUTH-009)*

---

## 12. Important end-to-end workflows

### WF-1 — Area setup (MVP)
Admin creates LocalBody → Village/Municipality → Wards → Officer and Worker
accounts. *(FR-VILL-001..003, FR-AUTH-003)*

### WF-2 — Asset registration (MVP)
Officer opens "register asset" → category, ward, installation date, status
(`WORKING`/…), condition (`GOOD`/…) → sets location via GPS or map (lat/long) →
adds 1–5 photos (auto-compressed) → optional warranty → saves; unique asset ID
assigned. *(FR-ASSET-001..008, FR-GIS-003, NFR-PERF-003)*

### WF-3 — Complaint to resolution (MVP, core)
Lifecycle and transitions:

| From | Action (actor) | To |
|------|----------------|----|
| — | Citizen submits complaint against a registered asset (description required, photo optional) | `PENDING` |
| `PENDING` | Officer rejects with reason | `REJECTED` (terminal) |
| `PENDING` | Officer sets priority + assigns one Worker | `ASSIGNED` |
| `ASSIGNED` | Officer reassigns to a different Worker | `ASSIGNED` |
| `ASSIGNED` | Worker taps "Start Repair" | `IN_PROGRESS` |
| `IN_PROGRESS` | Worker records completion photo (mandatory) + cost + remarks | `RESOLVED` |
| `RESOLVED` | Officer returns for rework with reason (counts as "returned") | `IN_PROGRESS` *(OQ-38)* |
| `RESOLVED` | Officer verifies | `VERIFIED` |
| `VERIFIED` | Officer closes (may update asset status/condition); history entry created | `CLOSED` |
| `CLOSED` | Citizen submits optional rating + optional one-time feedback | `CLOSED` |

Notifications fire in-app to the Citizen on every transition, to the Worker on
assign/reassign/return, and to the Officer on `RESOLVED`. Every transition is
audited. *(FR-COMP-004/006/008, FR-MAINT-001..010, FR-NOTIF-001..003, FR-HIST-001,
FR-FEED-001, FR-AUDIT-001)*

### WF-4 — Maintenance history accrual (MVP)
Each `CLOSED` complaint appends an entry to the asset's history (date, complaint
ref, remarks, worker, cost, completion photos, resulting status/condition), shown
newest-first on the asset detail view. *(FR-HIST-001..003, FR-ASSET-011)*

### WF-5 — Oversight (MVP subset)
Officer opens analytics → filters by ward/category/period → reviews asset
status/condition counts and complaint counts → generates the Complaint report and
Asset-register report as PDF/Excel. *(FR-ANLY-001/002, FR-RPT-001..003)*

### WF-6 — Expenditure & inventory (SECONDARY)
Worker enters actual repair cost on completion (MVP) → Officer reviews, categorizes,
and records official expenditure → expenditure rolls up against a ward budget
allocation; Worker optionally records materials consumed, decrementing the central
store. *(FR-MAINT-005/011, FR-BUD-001..004, FR-INV-001..005)*

---

## 13. MVP definition

The MVP is the smallest release that runs the **register → report → assign →
repair → verify → close → history → rate** loop end to end for one local body,
online, on Android, in English/Hindi.

**An MVP release must provide:**

- Auth & four roles with enforced permissions; citizen self-registration
  (mobile + password); admin-managed Officer/Worker accounts; admin password reset;
  basic self-contained citizen recovery; language selection; privacy statement.
  *(FR-AUTH-001..006, 009..012)*
- `LocalBody → Village/Municipality → Ward` setup; nothing hardcoded to one village.
  *(FR-VILL-001..006)*
- Asset registration: unique ID, Admin-configurable category, ward, lat/long,
  status (`WORKING`/`BROKEN`/`UNDER_MAINTENANCE`/`DECOMMISSIONED`), condition
  (`GOOD`/`FAIR`/`POOR`/`CRITICAL`), installation date, 1–5 compressed photos,
  optional warranty; edit; decommission; list/filter; detail view with history +
  open complaints. *(FR-ASSET-001..011)*
- Map: asset markers, marker → detail, set location by GPS/map, filter by
  category/status/ward. *(FR-GIS-001..004)*
- Complaints: registered Citizen files against a **registered asset**, description
  required, photo optional; own complaint list with status & resolution; lifecycle
  `PENDING→ASSIGNED→IN_PROGRESS→RESOLVED→VERIFIED→CLOSED` + `REJECTED`; Officer
  queue for the whole local body with filters; reject-with-reason; citizens cannot
  delete history. *(FR-COMP-001..008, 011)*
- Maintenance: Officer sets priority (`LOW/MEDIUM/HIGH/CRITICAL`) and assigns **one
  Worker**; reassignment; Worker sees only own tasks; "Start Repair"; completion
  requires a photo + cost + remarks; Officer verify → close, or return for rework;
  asset status/condition update on close; all transitions logged. *(FR-MAINT-001..010)*
- Maintenance history auto-created on close. *(FR-HIST-001..003)*
- Citizen feedback: optional rating (scale **OQ-37**) + optional one-time note on
  `CLOSED` complaints; visible to Officer/Admin and to the owning Worker.
  *(FR-FEED-001..005)*
- In-app notifications for all the transitions above. *(FR-NOTIF-001..003)*
- Audit log of important actions with actor/action/time/record/note; Admin view.
  *(FR-AUDIT-001..004)*
- Analytics: asset status & condition counts; complaint counts by
  status/ward/category/priority; Workers see only their own metrics.
  *(FR-ANLY-001/002)*
- Reports: Complaint report + Asset register report, exportable as **PDF and
  Excel**. *(FR-RPT-001..003)*
- NFRs: encrypted transit; **hashed passwords**; retention + privacy statement;
  rural-friendly bilingual UI; automatic photo compression; Android 8/API 26+;
  single Flutter app; multi-local-body data model; **daily PostgreSQL backup, ≥7
  days, media included, restore tested**. *(NFR-SEC-001..007, NFR-USE-001..005,
  NFR-I18N-001/002, NFR-PERF-003, NFR-COMPAT-001..004, NFR-SCAL-001/003,
  NFR-BAK-001..004)*

**Explicitly NOT in MVP:** inventory; budget/expenditure & allocation; advanced
analytics (resolution time, cost, worker-performance dashboard, trends); advanced
reports; SLA target reporting; citizen comment threads; complaint reopening;
proactive/general-area complaints; warranty alerts; duplicate linking; QR; offline;
web/desktop dashboard; SMS/WhatsApp/email/push; officer hierarchy/escalation;
crew assignment; multiple inventory stores; languages beyond English/Hindi; iOS
build; any AI feature; any external integration.

---

## 14. Secondary features (planned after MVP)

Inventory module — one central store (FR-INV-001..005, D29); official expenditure
recording & categorization (FR-MAINT-011, D30) and budget allocation vs spend
(FR-BUD-001..004, D31); advanced analytics (FR-ANLY-003..007) including the
objective Worker-performance metric set (D32); advanced reports
(FR-RPT-004..007); SLA **target reporting** with target-vs-actual, no escalation
(FR-SLA-001..003, D34 — target values still OQ-41); citizen comment threads
(FR-COMP-010, D17); duplicate-complaint linking (FR-COMP-009); proactive Officer/
Worker complaints (FR-COMP-013); general-area complaints (FR-COMP-014, D3 —
"future/secondary"); manual maintenance-history entries (FR-HIST-004);
warranty-expiry notifications (FR-NOTIF-004); map styling for open-complaint/
broken/decommissioned assets and ward overlays (FR-GIS-005/006); hard-delete
controls (FR-ASSET-012); Officer audit-log read access (FR-AUDIT-005); aggregate
rating analytics (FR-FEED-006).

---

## 15. Open questions — status

### 15.1 Resolved by this baseline

| OQ | Resolved by | Outcome |
|----|-------------|---------|
| OQ-01 leadership role | D1 | No leadership role; Officers use reports/analytics. |
| OQ-02 single vs multi local body | D19 | Multi-local-body data model; one body for evaluation. |
| OQ-03 web/desktop console | D5 | None in MVP; single Flutter app; web dashboard is Future. |
| OQ-04 Officer geographic scope | D6 | Officers cover the whole local body; ward **filtering** only; ward permissions Future. |
| OQ-05 multiple Officers / hierarchy | D14 | Multiple equal Officers; no hierarchy; escalation Future. |
| OQ-06 worker crew vs individual | D15 | One complaint → one individual Worker. |
| OQ-07 citizen accounts / anonymity | D2, D16 | Accounts required (mobile + password); no anonymous complaints. |
| OQ-08 complaint without asset | D3 | Every MVP complaint links a registered asset. |
| OQ-09 ward boundaries | D13 | Ward **ID** only in MVP; no polygons; optional centroid. |
| OQ-10 citizen comments | D17 | No comment thread in MVP (Secondary). |
| OQ-11 photo rules | D11, D25 | Asset 1–5 photos (≥1 recommended); complaint citizen photo optional; Worker completion photo mandatory; compression controls. |
| OQ-12 notification channels | D10, D20 | In-app only in MVP; external channels Future. |
| OQ-13 retention & privacy | D18 | Retain for deployment lifetime unless audited Admin removal; hashed passwords; no citizen self-deletion; simple privacy statement. |
| OQ-15 offline | D4 | MVP online-only; offline + sync Future. |
| OQ-16 credential recovery | D21 | Admin resets staff passwords; citizen recovery basic & self-contained (mechanism = OQ-40). |
| OQ-18 audit scope | D22 | Defined action list + actor/action/time/record/note; no click logging. |
| OQ-18a languages | D12 | English + Hindi, user-selectable; others Future. |
| OQ-19 category list | D23 | Admin-configurable, no code change; 12-item seed list. |
| OQ-20 external integrations | D20 | None in MVP; self-contained. |
| OQ-21 worker analytics visibility | D28 | Own task info only; no other workers / budget / village-wide / admin reports. |
| OQ-22 status & condition value sets | D8, D24 | Status {WORKING, BROKEN, UNDER_MAINTENANCE, DECOMMISSIONED}; Condition {GOOD, FAIR, POOR, CRITICAL}; separate concepts. |
| OQ-23 complaint photo mandatory | D11 | Citizen photo optional; Worker completion photo mandatory. |
| OQ-24 complaint lifecycle | D7 | PENDING→ASSIGNED→IN_PROGRESS→RESOLVED→VERIFIED→CLOSED; REJECTED from review. |
| OQ-26 priority levels | D9 | LOW, MEDIUM, HIGH, CRITICAL; Officer sets final; AI suggestion Future. |
| OQ-27 SLA enforcement | D34 | Reporting-only; no auto-escalation in MVP; escalation Future. |
| OQ-28 inventory stock locations | D29 | One central store initially; multiple stores Future. |
| OQ-29 repair cost vs budget | D30 | Worker records actual cost; Officer confirms/categorizes official expenditure. |
| OQ-30 budget allocations | D31 | Allocation vs spend vs remaining — Secondary. |
| OQ-31 worker performance definition | D32 | Objective operational metrics only; no subjective score. |
| OQ-32 report export formats | D33 | PDF + Excel; MVP = Complaint report + Asset register report. |
| OQ-33 QR roles | D35 | QR Future; role-dependent; no anonymous access. |
| OQ-34 backup frequency/retention | D36 | Daily PostgreSQL backup, ≥7 days, media included, restore tested; manual acceptable initially. |
| OQ-35 device / OS minimum | D26 | Android-first, Android 8.0 / API 26+; iOS possible later. |

### 15.2 Classification decided, detail intentionally deferred

| OQ | Status | Note |
|----|--------|------|
| OQ-25 complaint reopening | **DEFERRED** | Reopen is **Future/Secondary**. No detailed policy (window length, who may reopen, how many times, resulting state) is approved. Must not be implemented or assumed in MVP. Revisit when the feature is scheduled. |
| OQ-41 SLA target values per priority | **DEFERRED** | Needed only for the Secondary SLA feature. The "HIGH = 48h" figure in D34 is **illustrative, not approved**. Real targets to be set with the local body when SLA reporting is built. |

### 15.3 Still OPEN / PENDING (do not assume values)

| OQ | Status | Description | Why it is not blocking now |
|----|--------|-------------|----------------------------|
| **OQ-14** | OPEN | **System scale / capacity** — number of assets, number of users (per role), number of complaints (per month), photo/media volume. **No numeric capacity has been approved.** Any figure elsewhere in this doc is an assumption, not a decision. | Architecture can proceed with a scalable design (D19) and revisit sizing once figures exist. |
| **OQ-17** | OPEN | **MVP availability target** — no percentage / uptime SLA has been approved. **Do not invent "99%" or any number.** | Not needed to design the system; set before deployment planning. |
| **OQ-25** | OPEN (detail) | **Complaint reopening policy** — feature is Future/Secondary; the detailed policy is undefined and must be shown as such, not presented as approved. | Feature is out of MVP scope. |
| **OQ-36** | PENDING (external) | **University DSN3099 milestones / rubric / deadlines** — must come from faculty/course material. Remain PENDING until supplied. **No dates invented.** | Scheduling artifact, not a system requirement. |
| **OQ-37** | OPEN | **Citizen rating scale** (e.g. 1–5) and confirmation of the one-time feedback-text handling/visibility (FR-FEED-002/003). | Small; can be fixed before the feedback screen is built. |
| **OQ-38** | OPEN | **Lifecycle representation of a failed Officer verification** — return to `IN_PROGRESS` with a "returned" flag, or a distinct `RETURNED` state? D7 lists no returned state; D32 counts "tasks returned". | Modelling detail for the complaint state machine; resolve during architecture. |
| **OQ-39** | OPEN | **Org hierarchy semantics** — are "LocalBody" and "Village/Municipality" in D19 two genuinely distinct levels or effectively one? Affects the data model. | D19's hierarchy string is taken as given; confirm during data modeling. |
| **OQ-40** | OPEN | **Concrete mechanism for "basic, self-contained" citizen password recovery** with no external OTP/email/SMS (D21). Needs a secure approach that fits the constraint. | Design item; flag as a security-design risk. |
| **OQ-42** | OPEN | **Citizen recovery request persistence.** ADR-0007's approved staff-assisted flow (`POST /auth/citizen/recovery/request` followed by a staff `.../resolve` action) implies a pending recovery-request record, but `DATA_MODEL.md` defines no entity for it (`CITIZEN_RECOVERY` there is only the separate, optional recovery-code supplement, correctly not built). Unresolved: (A) treat the office/staff interaction as a manual process with no persisted pending-request record, or (B) persist recovery requests in CAMS — which requires `DATA_MODEL.md` to first define the entity, lifecycle, fields, security/retention rules, and relationships before Auth implementation. Neither option is chosen here; ADR-0007 is not reinterpreted. *(Identified 2026-09-12 during the baseline-schema architecture review.)* | Must be resolved before the Auth/User module is implemented (it is not needed for the schema alone). |

---

## 16. Requirements traceability structure

### 16.1 Identifier scheme
- Functional: `FR-<AREA>-<NNN>`, `<AREA>` ∈ {AUTH, VILL, ASSET, GIS, COMP, MAINT,
  HIST, FEED, NOTIF, AUDIT, INV, BUD, ANLY, RPT, SLA, QR}.
- Non-functional: `NFR-<AREA>-<NNN>`, `<AREA>` ∈ {SEC, PERF, REL, USE, I18N, MAINT,
  SCAL, BAK, COMPAT, OFF}.
- User stories `US-<ROLE>-<NN>` ({ADM, OFF, WRK, CIT}); workflows `WF-<N>`;
  decisions `D<n>`; open questions `OQ-<NN>`.
- IDs are **stable**: never reused or renumbered. Withdrawn/superseded items are
  kept in place and marked (see FR-AUTH-007).

### 16.2 Lifecycle status per requirement
`APPROVED` → `IN DESIGN` → `IN DEVELOPMENT` → `IMPLEMENTED` → `VERIFIED`
(or `DEFERRED` / `WITHDRAWN`). At baseline: all MVP and Secondary/Future
requirements here are **APPROVED** except those tagged **OPEN** in §15.3 (which stay
`OPEN` until answered).

### 16.3 Traceability matrix (to be maintained during the project)

| Req ID | Summary | Priority | Basis (D / brief) | Status | Design ref | Implementation ref | Test ref | Verified? |
|--------|---------|----------|-------------------|--------|-----------|--------------------|----------|-----------|
| FR-… | … | MVP/SEC/FUT | D… | APPROVED | — | — | — | No |

Kept in a dedicated file (e.g. `docs/requirements/TRACEABILITY.md`) or generated —
**not created yet**; this section only defines the structure.

### 16.4 Coverage rules
- Every MVP functional requirement must have ≥1 automated test before it is
  `VERIFIED` (NFR-MAINT-001).
- Every requirement traces to a decision (D1–D37), the brief, a user story, or a
  workflow.
- Every OPEN item in §15.3 must be resolved (recorded in `docs/decisions/`) before
  the requirements it affects move past `IN DESIGN`.

---

## Appendix A — Change log

| Version | Date | Notes |
|---------|------|-------|
| 0.1 (DRAFT) | 2026-09-04 | Initial draft from the project brief. All requirements PROPOSED; ~36 open questions. |
| **1.0 (BASELINE)** | 2026-09-04 | Requirements decision pass complete. Incorporated approved decisions **D1–D37**. Resolved OQ-01..OQ-13, OQ-15, OQ-16, OQ-18, OQ-18a, OQ-19..OQ-24, OQ-26..OQ-35. New sections: **§9.8 Citizen feedback & rating (FR-FEED)**, **§9.10 Audit trail (FR-AUDIT)**, **§9.15 SLA tracking (FR-SLA)**; new NFR groups **I18N** and **COMPAT**. Complaint lifecycle fixed to D7. Asset status (D8) and condition (D24) fixed and explicitly separated. Org hierarchy extended to `LocalBody → Village/Municipality → Ward` (D19). "Before" photo downgraded from required (v0.1) to optional; **completion photo now mandatory** (D11). Notifications constrained to in-app (D10). Platform fixed to single Flutter app / Android API 26+ (D5, D26). Still OPEN: **OQ-14** (scale), **OQ-17** (availability), **OQ-25** (reopen detail), **OQ-36** (university dates, PENDING); newly raised: **OQ-37** (rating scale), **OQ-38** (returned-state modelling), **OQ-39** (LocalBody vs Village/Municipality), **OQ-40** (citizen password recovery mechanism), **OQ-41** (SLA target values, deferred). No university dates invented. |

## Appendix B — Baseline sign-off

| Item | State |
|------|-------|
| Decisions D1–D37 incorporated | ✅ |
| Open questions resolved / classified / preserved | ✅ (see §15) |
| Internal MVP / Secondary / Future consistency check | ✅ (see §17 of the consolidation report) |
| University dates invented | ❌ none (OQ-36 PENDING) |
| Requirements version | **1.0 — BASELINE** |
| Next phase | Architecture / system design (gated: no implementation until architecture is approved) |
