/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.ssc.parser.fortifyaa;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanEntry;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.fortifyaa.parser.ScanParser;

class FortifyAAParserPluginTest {
	/** Sample Fortify Agentic Analyzer SARIF output, bundled under {@code sampleData}. */
	private static final String SAMPLE_FILE = "example.sarif";

	/** Minimal SARIF document declaring an unsupported spec version. */
	private static final String UNSUPPORTED_VERSION_SARIF =
			"{\"version\":\"2.0.0\",\"runs\":[]}";

	/** Backs a {@link ScanData} with a fixed classpath resource. */
	private final ScanData getScanData(String fileName) {
		return getScanData(() -> ClassLoader.getSystemResourceAsStream(fileName));
	}

	/** Backs a {@link ScanData} with an in-memory document. */
	private final ScanData getScanData(String content, boolean inline) {
		return getScanData(() -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}

	private interface InputStreamSupplier { InputStream get() throws IOException; }

	private final ScanData getScanData(InputStreamSupplier supplier) {
		return new ScanData() {

			@Override
			public String getSessionId() {
				return UUID.randomUUID().toString();
			}

			@Override
			public List<ScanEntry> getScanEntries() {
				return null;
			}

			@Override
			public InputStream getInputStream(Predicate<String> matcher) throws IOException {
				return supplier.get();
			}

			@Override
			public InputStream getInputStream(ScanEntry scanEntry) throws IOException {
				return supplier.get();
			}

			@Override
			public URL getUrl(ScanEntry scanEntry) {
				return null; // test data is stream-backed, never URL-backed
			}
		};
	}

	private final ScanBuilder scanBuilder = (ScanBuilder) Proxy.newProxyInstance(
			FortifyAAParserPluginTest.class.getClassLoader(),
			  new Class[] { ScanBuilder.class }, new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					System.err.println(method.getName()+": "+(args==null?null:Arrays.asList(args)));
					return null;
				}
			});

	private final VulnerabilityHandler vulnerabilityHandler = new VulnerabilityHandler() {

		@Override
		public StaticVulnerabilityBuilder startStaticVulnerability(String instanceId) {
			System.err.println("startStaticVulnerability: "+instanceId);
			return (StaticVulnerabilityBuilder) Proxy.newProxyInstance(
					FortifyAAParserPluginTest.class.getClassLoader(),
					  new Class[] { StaticVulnerabilityBuilder.class }, new InvocationHandler() {

						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							System.err.println(method.getName()+": "+(args==null?null:Arrays.asList(args)));
							return null;
						}
					});
		}
	};

	@Test
	void testParseScan() throws Exception {
		System.err.println("\n\n---- "+SAMPLE_FILE+" - parseScan");
		new FortifyAAParserPlugin().parseScan(getScanData(SAMPLE_FILE), scanBuilder);
		// TODO Check actual output
	}

	@Test
	void testParseVulnerabilities() throws Exception {
		System.err.println("\n\n---- "+SAMPLE_FILE+" - parseVulnerabilities");
		new FortifyAAParserPlugin().parseVulnerabilities(getScanData(SAMPLE_FILE), vulnerabilityHandler);
		// TODO Check actual output
	}

	@Test
	void testParseScanUnsupportedVersion() throws Exception {
		try {
			new FortifyAAParserPlugin().parseScan(getScanData(UNSUPPORTED_VERSION_SARIF, true), scanBuilder);
			fail("Parser plugin didn't throw exception for unsupported input file version");
		} catch (ScanParsingException e) {
			assertTrue(e.getMessage().startsWith(ScanParser.MSG_UNSUPPORTED_INPUT_FILE_VERSION), "Exception message starts with '"+ScanParser.MSG_UNSUPPORTED_INPUT_FILE_VERSION+"'");
		}
	}

}
