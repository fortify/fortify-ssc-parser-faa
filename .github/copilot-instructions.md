# Copilot Instructions for This Repository

> These instructions apply to any AI coding assistant working in this repo. They
> are the canonical project guidance.

## Project Purpose

This repository is the **Fortify SSC parser plugin for the Fortify Agentic
Analyzer (FAA)** — engine type **`FORTIFY_AA`**. It reads the SARIF 2.1.0 output
produced by FAA and displays those findings correctly in Fortify Software
Security Center (SSC).

FAA is OpenText's AI-powered SAST capability: a new *scan type* that finds
vulnerabilities complementary to traditional data-flow SAST, run infrequently
and risk-based rather than on every commit. The analyzer emits standard **SARIF
2.1.0** so one output can flow into multiple consumers (SSC via this plugin,
Fortify on Demand, GitHub code scanning).

## Scope of this repo

This repo contains **only the parser plugin**. The Fortify Agentic Analyzer
itself (the tool that *produces* the SARIF) lives in a **separate, private
repository** and is not part of this codebase. The two are intentionally
**loosely coupled** and communicate only through the **SARIF 2.1.0 contract** —
do not assume the analyzer's source is available here, and do not add references
to sibling folders or the analyzer's internal docs.

## What the plugin is

- **Runtime/language:** Java, built with Gradle (via the included Gradle Wrapper).
- **Nature:** a **lightly-customized derivative** of Fortify's standard SARIF
  parser plugin
  ([`fortify-ssc-parser-sarif`](https://github.com/fortify/fortify-ssc-parser-sarif)).
  Keep it a **simple derivative**; avoid divergence from upstream that isn't
  required by the FAA mapping. When in doubt about build/release/docs mechanics,
  look at how `fortify-ssc-parser-sarif` does it — this repo follows the same
  conventions.
- **Engine type:** `FORTIFY_AA` (declared in `src/main/resources/plugin.xml`,
  returned by the parser, and expected in the `scan.info` discriminator of the
  upload zip).
- **Java package:** `com.fortify.ssc.parser.fortifyaa`.

## How it reads SARIF (mapping highlights)

The plugin consumes the fixed, predictable shape of FAA's SARIF output:

- SARIF **rules are synthetic and per-instance** (1:1 with results; the rule id is
  the category slug + instance id). Rules carry the Fortify taxonomy metadata as
  bare parts in rule properties: `kingdom` / `Type` (Category) / optional `SubType`
  (Subcategory) — that is FAA's casing since 26.4. The plugin matches **rule**
  property keys case-insensitively (covering the pre-26.4 `Kingdom` / `Subtype`
  and any future casing corrections driven by FoD's case-sensitive reader);
  result property keys stay exact. The rule's `shortDescription.text`
  is the combined "Category: Subcategory" *display* string (for FoD/GitHub, which
  render only that member) — the plugin prefers the bare `Type` property for SSC's
  separate Category field, using `shortDescription` only as a fallback for
  pre-26.4 files (where it carried the bare category) and generic SARIF.
- **Content channels (since FAA 26.4):** `result.message` is the one-line summary;
  the description is `rule.fullDescription` and the remediation is `rule.help`
  (markdown members preferred — the text members may be FoD-shaped HTML). Legacy
  fallbacks keep pre-26.4 files parsing: `message.markdown` as the description,
  and the `summary` / `remediation` result properties.
- Priority derives from `result.properties` `impact`/`likelihood` (quadrant);
  `result.properties["fortify-severity"]` is the top-precedence fallback, ahead
  of `priority`, the rule's `security-severity`, and the SARIF `level`.
- Stable dedup via `fingerprints["fortify/instance-id"]`.
- One canonical sink location; the display snippet comes from `contextRegion`
  (its own `startLine` numbers the gutter), falling back to `region.snippet` +
  the legacy `snippetStartLine` property for pre-26.4 files. FAA's emitted
  window is 21 lines (sized for FoD's Code tab); this plugin trims rendering to
  at most ±5 lines around the affected line so the single abstract field stays
  readable. FAA also emits a single-step `codeFlow` (FoD display plumbing) —
  this plugin deliberately ignores `codeFlows`.
- Issue detail in SSC renders as Jsoup-sanitized HTML; the view template lives in
  `src/main/resources/viewtemplate/ViewTemplate.json`.

The plugin owns only *parsing and display* — all determinism (taxonomy
alignment, fingerprints, severity) is already baked into the SARIF by the
analyzer. The plugin should not try to re-derive or "fix" that data.

## Multi-Consumer Compatibility

The analyzer's single SARIF output is meant to stay usable across consumers, so
keep standard SARIF fields intact when changing parsing logic:

- **Fortify SSC** — via this plugin.
- **Fortify on Demand (FoD)** — via SARIF import.
- **GitHub code scanning** — relies on standard SARIF (`level`, rule metadata,
  stable `ruleId`).

## Working Conventions

- **SARIF version:** standardize on **SARIF 2.1.0**.
- Prefer decisions that keep parsing **reproducible and deterministic**.
- **Documentation is generated.** `README.md`, `USAGE.md`, `CONTRIBUTING.md`,
  `CODE_OF_CONDUCT.md` and `LICENSE.txt` are **auto-generated** from
  `doc-resources/` + shared templates by the `update-repo-docs` workflow (see
  `.github/workflows/`). **Edit the fragments in `doc-resources/`**
  (`repo-intro.md`, `repo-usage.md`, `repo-resources.md`, `repo-devinfo.md`,
  `parser-obtain-results.md`, `template-values.md`) — **never hand-edit the
  generated top-level files**; they carry a "do not edit by hand" footer and will
  be overwritten.
- **Versioning & releases:** handled by `release-please` from
  [Conventional Commits](https://www.conventionalcommits.org/). `version.txt` and
  `CHANGELOG.md` are managed by the release tooling; the production-release
  workflow builds and attaches the plugin jar. Use conventional commit messages
  (`fix:`, `feat:`, `docs:`, `chore:`, `ci:`, `feat!:` / `BREAKING-CHANGE:`).
  See "Versioning & manifest policy" below for how versions land in `plugin.xml`.
- **Build:** `./gradlew clean build` (tests included); `./gradlew dist
  distThirdParty` for the distributable plugin jar + third-party bundle. The
  plugin version is read from `version.txt` (overridable with `-Pversion=x.y.z`).
- **Instruction file location:** keep project guidance in
  `.github/copilot-instructions.md` (this file) and standard Copilot locations
  (`.github/instructions/**`). This is the single canonical source; point other
  assistants here rather than keeping a separate copy.

## Versioning & manifest policy (`plugin.xml`)

- **Manifest schema:** use **pluginmanifest-1.1** (the latest — there is no 1.2).
  We want 1.1 specifically for `<parser-type>STATIC</parser-type>`, which 1.0 lacks.
- **Plugin version** (`<version>`): automatic semantic versioning via release-please,
  deliberately **not** aligned with FAA's OpenText `[yy].[Q].[patch]` scheme. The
  build substitutes it into the `<!--VERSION-->` marker. The manifest's
  `PluginVersion` type allows **dotted digits only** (up to 4 groups, max 25 chars —
  no `-preview`-style suffixes), which plain `x.y.z` satisfies.
- **`api-version`:** never hardcode. The build derives it from the
  `com.fortify.plugin:plugin-api` dependency actually resolved on the compile
  classpath (declared explicitly in `build.gradle`, overriding the older pin from
  the shared `ssc-parser-plugin-helper`) and substitutes it into `plugin.xml`,
  truncated to `major.minor` to fit the manifest's `ApiVersion` pattern
  `(\d{1,2})(\.\d{1,4}){0,2}` (max 8 chars).
- **`data-version`:** an integer; increase it by exactly 1 whenever the FAA SARIF
  data format changes (or the plugin starts producing different data from the same
  input).
- **`supported-engine-versions`:** deliberately omitted. It is optional per the
  schema, SSC does not act on it, and FAA's release labels (e.g. `26.4-preview3`)
  cannot be expressed in its numeric range pattern.
- **FAA ↔ plugin version relationships:** whenever a plugin version or data version
  is tied to specific FAA versions, document that relationship in
  `plugin-info/description` in `plugin.xml` (e.g. "Data version 6 corresponds to
  the SARIF data format produced by FAA 26.4-preview3 and later").
