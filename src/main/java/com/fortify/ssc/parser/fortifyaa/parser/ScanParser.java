package com.fortify.ssc.parser.fortifyaa.parser;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;

/**
 * This class parses the SARIF JSON to set the various {@link ScanBuilder}
 * properties for the Fortify AA parser plugin.
 */
public class ScanParser {
	public static final String MSG_UNSUPPORTED_INPUT_FILE_VERSION = "Unsupported input file version";
	private final ScanData scanData;
    private final ScanBuilder scanBuilder;
    private String version;
    private int numFiles = 0;
    private String agent;
    private String agentVersion;
    private Integer elapsedSeconds;
    
	public ScanParser(final ScanData scanData, final ScanBuilder scanBuilder) {
		this.scanData = scanData;
		this.scanBuilder = scanBuilder;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		new SarifScanDataStreamingJsonParser()
			.handler("/version", jp -> version=jp.getValueAsString())
			.handler("/runs/*/invocations/*/endTimeUtc", jp -> scanBuilder.setScanDate(jp.readValueAs(Date.class)))
			.handler("/runs/*/invocations/*/machine", jp -> scanBuilder.setHostName(jp.getValueAsString()))
			.handler("/runs/*/invocations/*/properties/agent", jp -> agent=jp.getValueAsString())
			.handler("/runs/*/invocations/*/properties/agentVersion", jp -> agentVersion=jp.getValueAsString())
			.handler("/runs/*/invocations/*/properties/elapsedSeconds", jp -> elapsedSeconds=jp.getValueAsInt())
			.handler("/runs/*/automationDetails/guid", jp -> scanBuilder.setBuildId(jp.getValueAsString()))
			.handler("/runs/*/automationDetails/id", jp -> scanBuilder.setScanLabel(jp.getValueAsString()))
			.handler("/runs/*/artifacts", jp -> numFiles+=jp.countArrayEntries())
			.parse(scanData);
		
		if ( !"2.1.0".equals(version) ) {
			throw new ScanParsingException(MSG_UNSUPPORTED_INPUT_FILE_VERSION+": "+version);
		}
		scanBuilder.setEngineVersion(buildEngineVersion());
		if ( elapsedSeconds != null ) {
			scanBuilder.setElapsedTime(elapsedSeconds);
		}
		if ( numFiles > 0 ) {
			scanBuilder.setNumFiles(numFiles);
		}
		scanBuilder.completeScan();
	}
	
	private String buildEngineVersion() {
		StringBuilder sb = new StringBuilder();
		if ( StringUtils.isNotBlank(agent) ) {
			sb.append(agent);
			if ( StringUtils.isNotBlank(agentVersion) ) {
				sb.append(" ").append(agentVersion);
			}
		}
		String result = sb.toString();
		// engineVersion has max 80 chars in SSC
		if ( result.length() > 80 ) {
			result = result.substring(0, 77) + "...";
		}
		return result.isEmpty() ? "1.0.0" : result;
	}
}
