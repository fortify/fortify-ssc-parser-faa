# Changelog

## [1.1.0](https://github.com/fortify/fortify-ssc-parser-faa/compare/v1.0.1...v1.1.0) (2026-07-21)


### Features

* plugin-api 1.2.2320.0, build-derived api-version, data-version 6 ([f3e703c](https://github.com/fortify/fortify-ssc-parser-faa/commit/f3e703cc81ca17198da5c80d8b6d4428dd380321))
* prefer bare-category rule property Type; case-insensitive rule-property keys ([10c02b4](https://github.com/fortify/fortify-ssc-parser-faa/commit/10c02b4bfd03e40bdd7771dc18ebd96f6e460de7))
* read description/remediation from the per-instance rule channels; message is the one-line summary (FAA 26.4 format) ([297bf42](https://github.com/fortify/fortify-ssc-parser-faa/commit/297bf422b005fe8336de9552c2f8ec8e3129921c))
* support FAA 26.4 SARIF format (per-instance rules, contextRegion snippets, kingdom/SubType casing); bump data-version to 7 ([e789ca3](https://github.com/fortify/fortify-ssc-parser-faa/commit/e789ca350091f42bc83f912aba386b1e77e443bb))
* trim snippet rendering to 5 lines of context around the affected line ([d2f5bf2](https://github.com/fortify/fortify-ssc-parser-faa/commit/d2f5bf2ce7553fbdcad73073d3e574ab9a35f527))

## [1.0.1](https://github.com/fortify/fortify-ssc-parser-faa/compare/v1.0.0...v1.0.1) (2026-07-01)


### Bug Fixes

* align example.sarif with FAA 26.4-preview1 output ([43dac75](https://github.com/fortify/fortify-ssc-parser-faa/commit/43dac75476464460369b9540a98f3a79d87121d9))

## 1.0.0 (2026-06-29)


### Bug Fixes

* replace mapdb with in-memory collections to shrink jar ([06adfc7](https://github.com/fortify/fortify-ssc-parser-faa/commit/06adfc7cda6dfb1ee67e53dad89a5f263e607934))
* upgrade commons-io to 2.22.0 ([e7ca7e2](https://github.com/fortify/fortify-ssc-parser-faa/commit/e7ca7e29696be7c978ec40af69ada78fa4995744))
* upgrade commons-lang3 to 3.20.0 ([25685f1](https://github.com/fortify/fortify-ssc-parser-faa/commit/25685f1b3cf651448203f418b5404a4dfd09c722))
* upgrade jackson to 2.22.0 ([fbbc8a5](https://github.com/fortify/fortify-ssc-parser-faa/commit/fbbc8a5940ccc00b11a2130f8793e288e119f1b2))


### Miscellaneous Chores

* release 1.0.0 ([a67135c](https://github.com/fortify/fortify-ssc-parser-faa/commit/a67135c41d059a8b364b989e51571bf1b329c1d8))

## 0.9.0

In active development; not yet published. A changelog history will start once the
plugin reaches its first public release.
