This Fortify SSC parser plugin imports the results produced by the **Fortify
Agentic Analyzer (FAA)** — OpenText's AI-powered SAST capability that finds
vulnerabilities complementary to traditional data-flow SAST. The analyzer emits
its findings as **SARIF 2.1.0**; this plugin reads that SARIF and displays the
findings in Fortify Software Security Center (SSC) under the `FORTIFY_AA` engine
type.

The plugin is a lightly-customized derivative of Fortify's standard SARIF parser
plugin ([`fortify-ssc-parser-sarif`](https://github.com/fortify/fortify-ssc-parser-sarif)),
tailored to the specific, fixed shape of FAA's SARIF output (Fortify taxonomy
categories, `fortify-severity`, stable instance fingerprints, and a single
canonical sink location per finding).

### Limitations

* **SARIF 2.1.0 only**
  The plugin parses the SARIF 2.1.0 produced by the Fortify Agentic Analyzer.
  Other SARIF versions are rejected.

* **One engine type per application version**
  Due to limitations in the SSC parser framework, results from different scan
  engines cannot be merged into a single SSC application version. Upload FAA
  results to an application version dedicated to the `FORTIFY_AA` engine type.
