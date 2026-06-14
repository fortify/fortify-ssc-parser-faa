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

- SARIF **rules = Fortify Categories** (carrying `Kingdom` / optional `Subtype`
  taxonomy metadata); **results = specific findings**, with finding detail in
  `result.message.text` and `result.properties.remediation`.
- Severity is read from `result.properties["fortify-severity"]` (authoritative
  for SSC), in addition to the SARIF `level`.
- Stable dedup via `fingerprints["fortify/instance-id"]`.
- One canonical sink location with a short snippet; no `codeFlows` / `threadFlows`.
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
- **Build:** `./gradlew clean build` (tests included); `./gradlew dist
  distThirdParty` for the distributable plugin jar + third-party bundle. The
  plugin version is read from `version.txt` (overridable with `-Pversion=x.y.z`).
- **Instruction file location:** keep project guidance in
  `.github/copilot-instructions.md` (this file) and standard Copilot locations
  (`.github/instructions/**`). This is the single canonical source; point other
  assistants here rather than keeping a separate copy.
