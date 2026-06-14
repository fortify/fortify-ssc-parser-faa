Results consumed by this plugin are produced by the **Fortify Agentic Analyzer
(FAA)**. Running a scan over a source tree produces a SARIF 2.1.0 document,
already wrapped in a `.zip` together with a `scan.info` (`engineType=FORTIFY_AA`)
discriminator for direct SSC upload:

```
fortifyaa scan <source-dir> --output results.sarif --fpr <existing-fortify-results.fpr>
```

Refer to the Fortify Agentic Analyzer's own documentation for the full set of
scan options, policy files, and model configuration. A sample of the SARIF this
plugin consumes is included under [sampleData](sampleData).
