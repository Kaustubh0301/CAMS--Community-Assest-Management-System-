# ADR-0011 — Reporting (PDF + Excel) approach

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-13**.
**Date:** 2026-09-04 · **Review note:** approved as written. MVP reports = Complaint report + Asset register, in PDF and Excel; server-side generation with **Apache POI** (Excel) and **OpenPDF / simple PDF**. Advanced reports remain later scope (not in the requirements baseline as MVP).
**Relates to:** REQUIREMENTS.md D33, FR-RPT-001..003, FR-ANLY-001..002;
SYSTEM_ARCHITECTURE.md §13; API_ARCHITECTURE.md §4.10; team decision **AD-13**

## Context

MVP must generate **two** reports — **Complaint report** and **Asset register
report** — in **PDF and Excel** (D33). Advanced/scheduled reports are Secondary.
Analytics (FR-ANLY-001/002) are on-screen counts. The generator must be simple
enough for a student team and run on the single-host backend.

## Decision

- **Generate reports server-side** on request
  (`POST /reports/complaint`, `POST /reports/asset-register`, body carries filters
  + `format=PDF|XLSX`), returning a file stream. **Synchronous** for MVP volumes.
- **Excel:** **Apache POI** (`SXSSF` streaming workbook) — one sheet per report,
  a header block echoing the applied filters, then a table.
- **PDF:** a **simple tabular layout** via **OpenPDF** (LGPL/MPL) *or* an
  HTML-template → PDF approach (e.g. a templating engine + an HTML-to-PDF library).
  Team picks one during implementation; both produce a plain table + header. **No**
  pixel-perfect / letterhead templating for MVP.
- **Shared query layer:** reports and the analytics endpoints use the **same
  read queries/DTOs** so numbers match between the screen and the file.
- **Content (columns) as in SYSTEM_ARCHITECTURE §13** — e.g. Complaint report:
  id, asset, ward, category, status, priority, submitted, closed, resolution days,
  worker, cost, rating; Asset register: code, category, ward, status, condition,
  install date, #open complaints, last maintenance date.
- **Safety:** user-supplied text in cells is escaped against spreadsheet formula
  injection (leading `= + - @`) — SECURITY_ARCHITECTURE §8.

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **JasperReports / BIRT** | Powerful banded-report engines with a designer, but a heavy dependency and its own report-design skill; overkill for two plain tabular reports. Reconsider only if Secondary reporting grows a lot. |
| **Client-side generation in Flutter** | Excel generation on-device is fragile and inconsistent; reports are often produced/printed from a desktop; server-side keeps one implementation and one place to secure. |
| **CSV instead of XLSX** | D33 says Excel. CSV can be an easy *extra* later but is not the requirement. |
| **Headless-browser HTML→PDF (Puppeteer/Playwright)** | Great fidelity, but pulls in a browser runtime to install and run — disproportionate for simple tables on a small host. |
| **Asynchronous job queue for reports now** | Not needed at MVP volumes; adds a job store + polling endpoint. Documented as a **non-breaking later addition** if reports get slow (OQ-14). |

## Consequences

**Positive**
- Apache POI is the de-facto standard, well-documented, pure-Java — easy for the
  team to learn and explain.
- One shared query layer keeps analytics and report numbers consistent (avoids
  "the screen says 12, the PDF says 11").
- Minimal dependencies; runs in-process on the single host.

**Negative / trade-offs**
- **Synchronous generation** blocks a request thread; large reports at unknown
  scale (OQ-14) could be slow. Mitigation: filter-required reports, streaming POI,
  a row cap with a "narrow your filters" message, and the documented async path.
- Plain-table PDFs are not visually fancy; acceptable for MVP, and a template
  approach can improve them later without changing the API.
- POI memory use grows with row count; `SXSSF` streaming keeps it bounded.

**Follow-up**
- Pick the PDF library/approach during implementation and note it here.
- Add a report row cap + message to config.
