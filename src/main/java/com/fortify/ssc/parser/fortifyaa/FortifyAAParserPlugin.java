package com.fortify.ssc.parser.fortifyaa;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanEntry;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.plugin.spi.ParserPlugin;
import com.fortify.ssc.parser.fortifyaa.parser.ScanParser;
import com.fortify.ssc.parser.fortifyaa.parser.VulnerabilitiesParser;
import com.fortify.util.ssc.parser.ScanEntryHelper;

/**
 * Main {@link ParserPlugin} implementation for parsing Fortify Agentic Analyzer
 * SARIF output. This class defines the parser plugin SPI methods; actual parsing
 * is done by the appropriate dedicated parser classes.
 */
public class FortifyAAParserPlugin implements ParserPlugin<CustomVulnAttribute> {
    private static final Logger LOG = LoggerFactory.getLogger(FortifyAAParserPlugin.class);

    @Override
    public void start() throws Exception {
        LOG.info("Fortify AA parser plugin is starting");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Fortify AA parser plugin is stopping");
    }

    @Override
    public Class<CustomVulnAttribute> getVulnerabilityAttributesClass() {
        return CustomVulnAttribute.class;
    }

    @Override
    public void parseScan(final ScanData scanData, final ScanBuilder scanBuilder) throws ScanParsingException, IOException {
        new ScanParser(scanData, getScanEntry(scanData), scanBuilder).parse();
    }

	@Override
	public void parseVulnerabilities(final ScanData scanData, final VulnerabilityHandler vulnerabilityHandler) throws ScanParsingException, IOException {
		new VulnerabilitiesParser(scanData, getScanEntry(scanData), vulnerabilityHandler).parse();
	}

	private ScanEntry getScanEntry(final ScanData scanData) {
		return ScanEntryHelper.getScanEntryByName(scanData,
				name -> name.endsWith(".sarif") || name.endsWith(".json"));
	}
}
