
<!-- START-INCLUDE:repo-usage.md -->


<!-- START-INCLUDE:usage/h1.standard-parser-usage.md -->

<x-tag-head>
<x-tag-meta http-equiv="X-UA-Compatible" content="IE=edge"/>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/highlight.min.js"/>
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js" />
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.js" />
--></x-tag-script>

<x-tag-style><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/styles/github.min.css" />
--></x-tag-style>

<x-tag-style><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.css" />
--></x-tag-style>
</x-tag-head>

# Fortify SSC Parser Plugin for FAA - Usage

## Introduction


<!-- START-INCLUDE:p.marketing-intro.md -->

[Fortify Application Security](https://www.microfocus.com/en-us/solutions/application-security) provides your team with solutions to empower [DevSecOps](https://www.microfocus.com/en-us/cyberres/use-cases/devsecops) practices, enable [cloud transformation](https://www.microfocus.com/en-us/cyberres/use-cases/cloud-transformation), and secure your [software supply chain](https://www.microfocus.com/en-us/cyberres/use-cases/securing-the-software-supply-chain). As the sole Code Security solution with over two decades of expertise and acknowledged as a market leader by all major analysts, Fortify delivers the most adaptable, precise, and scalable AppSec platform available, supporting the breadth of tech you use and integrated into your preferred toolchain. We firmly believe that your great code [demands great security](https://www.microfocus.com/cyberres/application-security/developer-security), and with Fortify, go beyond 'check the box' security to achieve that.

<!-- END-INCLUDE:p.marketing-intro.md -->



<!-- START-INCLUDE:repo-intro.md -->

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

<!-- END-INCLUDE:repo-intro.md -->


## Plugin Installation

These sections describe how to install, upgrade and uninstall the parser plugin in SSC.

### Install & Upgrade

* Obtain the plugin binary jar file; either:
     * Download from the repository release page: https://github.com/fortify/fortify-ssc-parser-faa/releases
     * Build the plugin from source: https://github.com/fortify/fortify-ssc-parser-faa/blob/main/CONTRIBUTING.md
* If you already have another version of the plugin installed, first uninstall the previously  installed version of the plugin by following the steps under [Uninstall](#uninstall) below
* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Click the `NEW` button
	* Accept the warning
	* Upload the plugin jar file
	* Enable the plugin by clicking the `ENABLE` button
  
### Uninstall

* In Fortify Software Security Center:
     * Navigate to Administration->Plugins->Parsers
     * Select the parser plugin that you want to uninstall
     * Click the `DISABLE` button
     * Click the `REMOVE` button 

## Obtain results


<!-- START-INCLUDE:parser-obtain-results.md -->

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

<!-- END-INCLUDE:parser-obtain-results.md -->


## Upload results

Results can be uploaded through the SSC web interface, REST API, or SSC client utilities like FortifyClient or [fcli](https://github.com/fortify-ps/fcli). The SSC web interface, FortifyClient and most other Fortify clients require the raw results to be packaged into a zip-file; REST API and fcli allow for uploading raw results directly.

To upload results through the SSC web interface or most clients:

* Create a `scan.info` file containing a single line as follows:   
     `engineType=FORTIFY_AA`
* Create a zip file containing the following:
	* The scan.info file generated in the previous step
	* The raw results file as obtained from the target system (see [Obtain results](#obtain-results) section above)
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient or Maven plugin
	* Or using the SSC web interface
	* Similar to how you would upload an FPR file
	
Both SSC REST API and fcli provide options for specifying the engine type directly, and as such it is not necessary to package the raw results into a zip-file with accompanying `scan.info` file. For example, fcli allows for uploading raw scan results using a command like the following:

`fcli ssc artifact upload -f <raw-results-file> --appversion MyApp:MyVersion --engine-type FORTIFY_AA`

<!-- END-INCLUDE:usage/h1.standard-parser-usage.md -->


<!-- END-INCLUDE:repo-usage.md -->


---

*[This document was auto-generated from USAGE.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
