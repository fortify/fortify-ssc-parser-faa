package com.fortify.ssc.parser.fortifyaa.parser;

import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.fortifyaa.CustomVulnAttribute;
import com.fortify.ssc.parser.fortifyaa.domain.Kind;
import com.fortify.ssc.parser.fortifyaa.domain.ReportingDescriptor;
import com.fortify.ssc.parser.fortifyaa.domain.Result;
import com.fortify.ssc.parser.fortifyaa.domain.RunData;
import com.fortify.ssc.parser.fortifyaa.util.MarkdownUtil;
import com.fortify.util.ssc.parser.EngineTypeHelper;
import com.fortify.util.ssc.parser.HandleDuplicateIdVulnerabilityHandler;

public final class VulnerabilitiesProducer {
	private static final Logger LOG = LoggerFactory.getLogger(VulnerabilitiesProducer.class);
	private static final String ENGINE_TYPE = EngineTypeHelper.getEngineType();
	private final VulnerabilityHandler vulnerabilityHandler;
	
	/**
	 * Constructor for storing {@link VulnerabilityHandler} instance.
	  * @param vulnerabilityHandler
	 */
	public VulnerabilitiesProducer(final VulnerabilityHandler vulnerabilityHandler) {
		this.vulnerabilityHandler = new HandleDuplicateIdVulnerabilityHandler(vulnerabilityHandler);
	}
	
	/**
	 * This method produces a Fortify vulnerability based on the given
	 * {@link ResultWrapperWithRunData} instance. No vulnerability will be produced 
	 * if {@link ResultWrapperWithRunData#resolveLevel()} returns a level that
	 * indicates that the result is not interesting from a Fortify perspective.
	 * @param result
	 */
	@SuppressWarnings("deprecation") // SSC JavaDoc states that severity is mandatory, but method is deprecated
	public final void produceVulnerability(RunData runData, Result result) {
		Kind kind = result.getKind();
		if ( kind == null ) {
			// SARIF specification says that if kind is not specified, then the default value of fail is to be used
			kind = Kind.fail;
		}
		switch(kind) {
			case review:
			case open:
			case fail:
				break;
			case informational:
			case notApplicable:
			case pass:
				// results with these kind values are not vulnerabilities.
				return;
		}
		Priority priority = getPriority(runData, result);
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getInstanceId(runData, result));
			
			// Set meta-data
			vb.setEngineType(ENGINE_TYPE);
			vb.setKingdom(getKingdom(runData, result));
			vb.setAnalyzer(getAnalyzer(runData, result));
			vb.setCategory(getCategory(runData, result));
			vb.setSubCategory(getSubCategory(runData, result));
			
			// Set mandatory values to JavaDoc-recommended values
			vb.setAccuracy(getAccuracy(runData, result));
			vb.setSeverity(getSeverity(runData, result));
			vb.setConfidence(getConfidence(runData, result));
			vb.setProbability(getProbability(runData, result));
			vb.setImpact(getImpact(runData, result));
			vb.setLikelihood(getLikelihood(runData, result));
			
			// Set standard vulnerability fields based on input
			vb.setFileName(getFileName(runData, result));
			vb.setPriority(priority);
			vb.setRuleGuid(getRuleGuid(runData, result));
			vb.setVulnerabilityAbstract(getAbstractHtml(runData, result));
			
			//vb.setClassName(null);
    		//vb.setFunctionName(functionName);
    		vb.setLineNumber(result.resolveLineNumber());
    		//vb.setMappedCategory(mappedCategory);
    		//vb.setMinVirtualCallConfidence(minVirtualCallConfidence);
    		//vb.setPackageName(packageName);
    		//vb.setRemediationConstant(remediationConstant);
    		//vb.setRuleGuid(ruleGuid);
    		//vb.setSink(sink);
    		//vb.setSinkContext(sinkContext);
    		//vb.setSource(source);
    		//vb.setSourceContext(sourceContext);
    		//vb.setSourceFile(sourceFile);
    		//vb.setSourceLine(sourceLine);
    		//vb.setTaintFlag(taintFlag);
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
			
			//vb.set*CustomAttributeValue(...)
			
			vb.setStringCustomAttributeValue(CustomVulnAttribute.categoryAndSubCategory, getCategoryAndSubCategory(runData, result));
			vb.setStringCustomAttributeValue(CustomVulnAttribute.summary, getSummary(runData, result));
			String remediationSummary = getRemediationSummary(runData, result);
			if ( StringUtils.isNotBlank(remediationSummary) ) {
				vb.setStringCustomAttributeValue(CustomVulnAttribute.remediationSummary, remediationSummary);
			}
    		
    		vb.completeVulnerability();
		}
	}

	private String getVulnerabilityAbstract(RunData runData, Result result) {
		return StringUtils.defaultIfBlank(result.getResultMessage(runData), "Not Available");
	}

	// Rich description: prefer the per-instance rule's fullDescription markdown — the
	// description's only carrier since FAA 26.4 (the result message is just the
	// one-line summary there). Fall back to the channels that carried the description
	// in pre-26.4 files: message markdown, then the resolved plain-text message.
	// Rendered to (whitelisted) HTML for SSC.
	private String getDescriptionHtml(RunData runData, Result result) {
		String source = getRuleFullDescriptionMarkdown(runData, result);
		if ( StringUtils.isBlank(source) ) {
			source = result.getMessage()==null ? null : result.getMessage().getMarkdown();
		}
		if ( StringUtils.isBlank(source) ) {
			source = result.getResultMessage(runData);
		}
		String html = MarkdownUtil.toHtml(source);
		return StringUtils.isBlank(html) ? "Not Available" : html;
	}

	// Everything shown in SSC goes into the single built-in long-text field that
	// custom parser plugins can actually write: vulnerabilityAbstract (brief).
	// (SSC caps custom attributes at 255 chars here, and setVulnerabilityRecommendation
	// is not persisted for plugin issues, so brief is our only long channel.)
	// Sections are separated with bold headers (basic HTML whitelist: no <h*>).
	private String getAbstractHtml(RunData runData, Result result) {
		StringBuilder sb = new StringBuilder();
		sb.append(getDescriptionHtml(runData, result));
		String snippet = getSnippetHtml(result);
		if ( StringUtils.isNotBlank(snippet) ) {
			Integer line = result.resolveLineNumber();
			sb.append("<p><strong>Affected code")
			  .append(line != null ? " (line " + line + ")" : "")
			  .append("</strong></p>").append(snippet);
		}
		// Remediation carrier since FAA 26.4: rule.help markdown; the legacy
		// "remediation" result property is read as a fallback for pre-26.4 files.
		String remediation = getRuleHelpMarkdown(runData, result);
		if ( StringUtils.isBlank(remediation) ) {
			remediation = getStringProperty(result.getProperties(), "remediation", null);
		}
		String remediationHtml = MarkdownUtil.toHtml(remediation);
		if ( StringUtils.isNotBlank(remediationHtml) ) {
			sb.append("<p><strong>Remediation</strong></p>").append(remediationHtml);
		}
		return sb.toString();
	}

	// Render the code snippet as a monospaced <pre><code> block with right-aligned
	// line numbers and the affected line in bold. Returns "" when there is no snippet.
	private String getSnippetHtml(Result result) {
		String snippet = result.resolveSnippet();
		if ( StringUtils.isBlank(snippet) ) { return ""; }
		snippet = StringUtils.stripEnd(snippet, "\n");
		// First line number of the snippet, in precedence order: the contextRegion's own
		// startLine (standard encoding, FAA 26.4+), the legacy non-standard
		// snippetStartLine property (pre-26.4 FAA windows), the region's startLine (a
		// region snippet spans the region per the SARIF spec). Without any, render
		// without a line-number gutter.
		Integer snippetStartLine = result.resolveContextRegionStartLine();
		if ( snippetStartLine == null ) {
			snippetStartLine = getIntegerProperty(result.getProperties(), "snippetStartLine");
		}
		if ( snippetStartLine == null ) {
			snippetStartLine = result.resolveLineNumber();
		}
		if ( snippetStartLine == null ) {
			return "<pre><code>" + escapeHtml(snippet) + "</code></pre>";
		}
		Integer affectedLine = result.resolveLineNumber();
		String[] lines = snippet.split("\n", -1);
		int width = String.valueOf(snippetStartLine + lines.length - 1).length();
		StringBuilder sb = new StringBuilder("<pre><code>");
		for ( int i = 0; i < lines.length; i++ ) {
			int lineNumber = snippetStartLine + i;
			String rendered = escapeHtml(String.format("%" + width + "d  %s", lineNumber, lines[i]));
			if ( affectedLine != null && lineNumber == affectedLine.intValue() ) {
				sb.append("<strong>").append(rendered).append("</strong>");
			} else {
				sb.append(rendered);
			}
			if ( i < lines.length - 1 ) { sb.append('\n'); }
		}
		return sb.append("</code></pre>").toString();
	}

	private static String escapeHtml(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private Integer getIntegerProperty(Map<String, Object> properties, String key) {
		String valueString = getStringProperty(properties, key, null);
		if ( StringUtils.isNotBlank(valueString) ) {
			try {
				return (int) Float.parseFloat(valueString);
			} catch (NumberFormatException nfe) {
				LOG.warn("Error converting {} string '{}' to integer: {}", key, valueString, nfe.getMessage());
			}
		}
		return null;
	}

	private String getFileName(RunData runData, Result result) {
		return result.resolveFullFileName(runData, "Unknown");
	}

	private String getInstanceId(RunData runData, Result result) {
		// Prefer the analyzer's own fingerprint (fortify/instance-id, an MD5) as the
		// SSC instance id, so there is a single, identical id in both the SARIF and
		// SSC. Fall back to a SHA-256 of the normalized id string only for generic
		// SARIF that doesn't carry our fingerprint.
		Map<String, String> fingerprints = result.getFingerprints();
		if ( fingerprints != null ) {
			String fortifyInstanceId = fingerprints.get("fortify/instance-id");
			if ( StringUtils.isNotBlank(fortifyInstanceId) ) {
				return fortifyInstanceId;
			}
		}
		return DigestUtils.sha256Hex(getInstanceIdString(runData, result));
	}
	
	private String getInstanceIdString(RunData runData, Result result) {
		if ( StringUtils.isNotBlank(result.getGuid()) ) {
			return result.getGuid();
		} else if ( StringUtils.isNotBlank(result.getCorrelationGuid()) ) {
			return result.getCorrelationGuid();
		} else if ( result.getFingerprints()!=null && result.getFingerprints().size()>0 ) {
			return new TreeMap<>(result.getFingerprints()).toString();
		} else {
			return generateInstanceIdString(runData, result);
		}
	}
	
	// As described at https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317932
	// we calculate a unique id string based on tool name, full file location,
	// rule id and partial finger prints if available. To increase chances
	// of generating a unique id, we also include the result message.
	// However, this could potentially still result in duplicate id strings. 
	// Possibly we could add information from other properties like region, 
	// logical location or code flows, but these may either not be available, or 
	// still result in duplicate uuid strings.
	private String generateInstanceIdString(RunData runData, Result result) {
		String partialFingerPrints = result.getPartialFingerprints()==null?"":new TreeMap<>(result.getPartialFingerprints()).toString();
		return String.join("|",
			StringUtils.defaultString(runData.getToolName()),
			getFileName(runData, result),
			StringUtils.defaultString(result.resolveRuleId(runData)),
			partialFingerPrints,
			getVulnerabilityAbstract(runData, result));
	}
	
	// Rule-property lookups are case-insensitive (see getRuleProperties), so this
	// reads FAA's current lowercase "kingdom" as well as the pre-26.4 "Kingdom".
	private String getKingdom(RunData runData, Result result) {
		String kingdom = getStringProperty(result.getProperties(), "kingdom", null);
		if ( StringUtils.isBlank(kingdom) ) {
			kingdom = getStringProperty(getRuleProperties(runData, result), "kingdom", null);
		}
		return kingdom;
	}
	
	// Since FAA 26.4, the rule's shortDescription.text carries the combined
	// "Category: Subcategory" DISPLAY string (for FoD and GitHub, which render only
	// that member), while the bare Category travels in the rule property "Type"
	// (FVDL naming). SSC keeps Category and Subcategory as separate fields and
	// builds its own combined display, so the bare "Type" property is preferred;
	// shortDescription remains a fallback for pre-26.4 FAA files (where it carried
	// the bare category) and for generic SARIF.
	private String getCategory(RunData runData, Result result) {
		String category = null;
		ReportingDescriptor rule = result.resolveRule(runData);
		if ( rule != null ) {
			category = getStringProperty(getRuleProperties(rule), "Type", null);
			if ( StringUtils.isBlank(category) && rule.getShortDescription() != null ) {
				category = result.resolveMessage(rule.getShortDescription(), runData);
			}
			if ( StringUtils.isBlank(category) && StringUtils.isNotBlank(rule.getName()) ) {
				if ( rule.getName().contains(StringUtils.SPACE)) {
					category = rule.getName();
				}else {
					category = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
				}
			}
		}
		if ( StringUtils.isBlank(category) ) {
			category = result.resolveRuleId(runData);
		}
		if ( StringUtils.isBlank(category) ) {
			category = StringUtils.defaultIfBlank(runData.getToolName(), "Unknown");
		}
		return category;
	}
	
	// Case-insensitive rule-property lookup covers both FAA's current "SubType"
	// (FoD's casing since 26.4) and the pre-26.4 "Subtype".
	private String getSubCategory(RunData runData, Result result) {
		return getStringProperty(getRuleProperties(runData, result), "SubType", null);
	}
	
	private String getAnalyzer(RunData runData, Result result) {
		return "FORTIFY_AA";
	}

	private float getAccuracy(RunData runData, Result result) {
		return getFloatProperty(getRuleProperties(runData, result), "Accuracy", 5.0f);
	}
	
	// Legacy "Severity" axis maps to Impact for the agentic analyzer.
	private float getSeverity(RunData runData, Result result) {
		return getImpact(runData, result);
	}

	// Legacy "Confidence" axis maps to Likelihood for the agentic analyzer.
	private float getConfidence(RunData runData, Result result) {
		return getLikelihood(runData, result);
	}

	private float getProbability(RunData runData, Result result) {
		return getFloatProperty(getRuleProperties(runData, result), "Probability", 2.5f);
	}

	private float getImpact(RunData runData, Result result) {
		Float impact = getFloatPropertyOrNull(result.getProperties(), "impact");
		return impact != null ? impact : getFloatProperty(getRuleProperties(runData, result), "Impact", 2.5f);
	}

	private float getLikelihood(RunData runData, Result result) {
		Float likelihood = getFloatPropertyOrNull(result.getProperties(), "likelihood");
		return likelihood != null ? likelihood : 2.5f;
	}

	// Primary path for the agentic analyzer: derive the Fortify Priority Order
	// quadrant from the impact/likelihood axes (see prioritization whitepaper).
	// Falls back to the standard SARIF precedence for generic SARIF input that
	// does not provide impact/likelihood.
	private Priority getPriority(RunData runData, Result result) {
		Float impact = getFloatPropertyOrNull(result.getProperties(), "impact");
		Float likelihood = getFloatPropertyOrNull(result.getProperties(), "likelihood");
		if ( impact != null && likelihood != null ) {
			return toQuadrantPriority(impact, likelihood);
		}
		return tryParsePriority("fortify-severity", result.getProperties())
			.orElseGet(() -> tryParsePriority("priority", result.getProperties())
			.orElseGet(() -> resolveSecuritySeverityPriority(runData, result)
			.orElseGet(() -> result.resolveLevel(runData).getFortifyPriority())));
	}

	// Fortify risk quadrant; the high/low threshold is 2.5 on the 0.1-5.0 scale.
	private Priority toQuadrantPriority(float impact, float likelihood) {
		boolean highImpact = impact >= 2.5f;
		boolean highLikelihood = likelihood >= 2.5f;
		if ( highImpact && highLikelihood ) { return Priority.Critical; }
		if ( highImpact ) { return Priority.High; }
		if ( highLikelihood ) { return Priority.Medium; }
		return Priority.Low;
	}

	private Optional<Priority> tryParsePriority(String propertyName, Map<String, Object> properties) {
		String value = getStringProperty(properties, propertyName, null);
		if ( StringUtils.isNotBlank(value) ) {
			try {
				return Optional.of(Priority.valueOf(value));
			} catch (IllegalArgumentException iae) {
				LOG.warn("Ignoring {}: '{}' is not a valid Fortify priority value", propertyName, value);
			}
		}
		return Optional.empty();
	}

	private Optional<Priority> resolveSecuritySeverityPriority(RunData runData, Result result) {
		String value = getStringProperty(getRuleProperties(runData, result), "security-severity", null);
		if ( StringUtils.isNotBlank(value) ) {
			try {
				float score = Float.parseFloat(value);
				// CVSS score range mapping from https://nvd.nist.gov/vuln-metrics/cvss
				if ( score < 0 ) {
					LOG.warn("Ignoring security-severity: {} is less than 0.", score);
				} else if ( score < 4 ) {
					return Optional.of(Priority.Low);
				} else if ( score < 7 ) {
					return Optional.of(Priority.Medium);
				} else if ( score < 9 ) {
					return Optional.of(Priority.High);
				} else if ( score <= 10 ) {
					return Optional.of(Priority.Critical);
				} else {
					LOG.warn("Ignoring security-severity: {} is greater than 10.", score);
				}
			} catch (NumberFormatException nfe) {
				LOG.warn("Ignoring security-severity: '{}' is not a valid float value", value);
			}
		}
		return Optional.empty();
	}

	// Our SARIF rule ids are a technical artifact, not real Fortify rules, so we
	// surface "N/A" as the Primary Rule ID rather than an opaque/empty value.
	private String getRuleGuid(RunData runData, Result result) {
		String fortifyRuleId = getStringProperty(result.getProperties(), "fortifyRuleId", null);
		if ( StringUtils.isNotBlank(fortifyRuleId) ) {
			return fortifyRuleId;
		}
		String ruleGuid = result.resolveRuleGuid(runData);
		return StringUtils.isNotBlank(ruleGuid) ? ruleGuid : "N/A";
	}

	private String getCategoryAndSubCategory(RunData runData, Result result) {
		String category = getCategory(runData, result);
		String subCategory = getSubCategory(runData, result);
		return StringUtils.isBlank(subCategory) ? category : String.join(": ", category, subCategory);
	}

	// Short (<=255 char) one-line description for the Issue Details panel. Since FAA
	// 26.4 the result message IS the one-line summary; its markdown member is
	// preferred because the text member may be FoD-shaped HTML. The legacy "summary"
	// result property is read first so pre-26.4 files keep their behavior.
	private String getSummary(RunData runData, Result result) {
		String summary = getStringProperty(result.getProperties(), "summary", null);
		if ( StringUtils.isBlank(summary) ) {
			summary = result.getMessage()==null ? null : result.getMessage().getMarkdown();
		}
		if ( StringUtils.isBlank(summary) ) {
			summary = result.getResultMessage(runData);
		}
		return truncateToMax(summary, 255);
	}

	// Short (<=255 char) remediation summary for the Issue Details panel.
	// Prefers an analyzer-provided summary; falls back to the (truncated) remediation
	// (legacy "remediation" property for pre-26.4 files, rule.help markdown since 26.4).
	private String getRemediationSummary(RunData runData, Result result) {
		String summary = getStringProperty(result.getProperties(), "remediationSummary", null);
		if ( StringUtils.isBlank(summary) ) {
			summary = getStringProperty(result.getProperties(), "remediation", null);
		}
		if ( StringUtils.isBlank(summary) ) {
			summary = getRuleHelpMarkdown(runData, result);
		}
		return truncateToMax(summary, 255);
	}

	// The description/remediation carriers since FAA 26.4: the per-instance rule's
	// fullDescription/help markdown members (the text members may be FoD-shaped HTML).
	private String getRuleFullDescriptionMarkdown(RunData runData, Result result) {
		ReportingDescriptor rule = result.resolveRule(runData);
		return rule==null || rule.getFullDescription()==null ? null : rule.getFullDescription().getMarkdown();
	}

	private String getRuleHelpMarkdown(RunData runData, Result result) {
		ReportingDescriptor rule = result.resolveRule(runData);
		return rule==null || rule.getHelp()==null ? null : rule.getHelp().getMarkdown();
	}

	// Trim to at most maxLength chars (appending an ellipsis when truncated) so a
	// value never hits SSC's hard 255-char custom-attribute cap mid-word.
	private String truncateToMax(String value, int maxLength) {
		if ( value == null ) { return null; }
		value = value.trim();
		return value.length() <= maxLength ? value : value.substring(0, maxLength - 3).trim() + "...";
	}
	
	private Float getFloatPropertyOrNull(Map<String, Object> properties, String key) {
		String valueString = getStringProperty(properties, key, null);
		if ( StringUtils.isNotBlank(valueString) ) {
			try {
				return Float.parseFloat(valueString);
			} catch (NumberFormatException nfe) {
				LOG.warn("Error converting {} string '{}' to float: {}", key, valueString, nfe.getMessage());
			}
		}
		return null;
	}

	private float getFloatProperty(Map<String, Object> properties, String key, float defaultValue) {
		String valueString = getStringProperty(properties, key, null);
		if ( StringUtils.isNotBlank(valueString) ) {
			try {
				return Float.parseFloat(valueString);
			} catch (NumberFormatException nfe) {
				LOG.warn("Error converting {} string '{}' to float: {}", key, valueString, nfe.getMessage());
			}
		}
		return defaultValue;
	}
	
	private String getStringProperty(Map<String, Object> properties, String key, String defaultValue) {
		if ( properties!=null && properties.containsKey(key) && properties.get(key) != null ) {
			return properties.get(key).toString();
		}
		return defaultValue;
	}
	
	private List<String> getStringListProperty(Map<String, Object> properties, String key, List<String> defaultValue) {
		if ( properties!=null && properties.containsKey(key) && properties.get(key) instanceof List ) {
			return ((List<?>) properties.get(key)).stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}
		return defaultValue;
	}

	// Rule property keys are matched case-insensitively. FAA's rule-property casing
	// has been corrected before to satisfy FoD's case-sensitive reader (Kingdom →
	// kingdom, Subtype → SubType), and further corrections on that channel must not
	// silently break this plugin. If a rule carried keys differing only in case
	// (never produced by FAA), one entry wins arbitrarily.
	private Map<String, Object> getRuleProperties(ReportingDescriptor rule) {
		Map<String, Object> properties = rule==null ? null : rule.getProperties();
		if ( properties == null ) { return null; }
		Map<String, Object> caseInsensitiveProperties = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		caseInsensitiveProperties.putAll(properties);
		return caseInsensitiveProperties;
	}
	
	private Map<String, Object> getRuleProperties(RunData runData, Result result) {
		return getRuleProperties(result.resolveRule(runData));
	}

}
