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
package com.fortify.ssc.parser.fortifyaa.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Result result(String json) throws Exception {
		return MAPPER.readValue(json, Result.class);
	}

	@Test
	void testResolveSnippetFromContextRegion() throws Exception {
		// Current FAA encoding: region = the sink line, contextRegion = the window.
		Result result = result("{\"locations\":[{\"physicalLocation\":{"
				+ "\"region\":{\"startLine\":21,\"endLine\":21,\"snippet\":{\"text\":\"sink line\"}},"
				+ "\"contextRegion\":{\"startLine\":16,\"endLine\":26,\"snippet\":{\"text\":\"window\\nsink line\\nmore\"}}}}]}");
		assertEquals("window\nsink line\nmore", result.resolveSnippet());
		assertEquals(16, result.resolveContextRegionStartLine());
		assertEquals(21, result.resolveLineNumber());
	}

	@Test
	void testResolveSnippetLegacyRegionWindow() throws Exception {
		// Legacy FAA encoding: region.snippet is the window; no contextRegion.
		Result result = result("{\"locations\":[{\"physicalLocation\":{"
				+ "\"region\":{\"startLine\":21,\"snippet\":{\"text\":\"window\\nsink line\\nmore\"}}}}]}");
		assertEquals("window\nsink line\nmore", result.resolveSnippet());
		assertNull(result.resolveContextRegionStartLine());
	}

	@Test
	void testResolveSnippetSingleLineRegionWithoutContext() throws Exception {
		// Current encoding at a one-line file: no contextRegion (it must be a proper superset).
		Result result = result("{\"locations\":[{\"physicalLocation\":{"
				+ "\"region\":{\"startLine\":3,\"endLine\":3,\"snippet\":{\"text\":\"sink line\"}}}}]}");
		assertEquals("sink line", result.resolveSnippet());
		assertNull(result.resolveContextRegionStartLine());
		assertEquals(3, result.resolveLineNumber());
	}

	@Test
	void testResolveSnippetAbsent() throws Exception {
		Result result = result("{\"locations\":[{\"physicalLocation\":{\"region\":{\"startLine\":3}}}]}");
		assertNull(result.resolveSnippet());
		assertNull(result.resolveContextRegionStartLine());
	}
	@Test
	void testReplaceLinks() {
		assertEquals("Prohibited term used in para[0]\\spans[2].", 
				Result.replaceLinks("Prohibited term used in [para\\[0\\]\\\\spans\\[2\\]](1).", null));
	}
	
	@Test
	void testReplaceArgs() {
		assertResolveArgs("Hello {}", null, "Hello {}");
		assertResolveArgs("Hello {}", new String[] {}, "Hello {}");
		assertResolveArgs("Hello {}", new String[] {"r1", "r2", "r3"}, "Hello {}");
		
		assertResolveArgs("Hello {0}, {1}", null, "Hello {0}, {1}");
		assertResolveArgs("Hello {0}, {1}", new String[] {}, "Hello {0}, {1}");
		assertResolveArgs("Hello {0}, {1}", new String[] {"r1", "r2", "r3"}, "Hello r1, r2");
		
		assertResolveArgs("Hello {0}, {1}, {2}, {3}, {4}", null, "Hello {0}, {1}, {2}, {3}, {4}");
		assertResolveArgs("Hello {0}, {1}, {2}, {3}, {4}", new String[] {}, "Hello {0}, {1}, {2}, {3}, {4}");
		assertResolveArgs("Hello {0}, {1}, {2}, {3}, {4}", new String[] {"r1", "r2", "r3"}, "Hello r1, r2, r3, {3}, {4}");
		
		assertResolveArgs("triple braces '{{{...}}}' or amp", null, "triple braces '{{{...}}}' or amp");
		assertResolveArgs("triple braces '{{{...}}}' or amp", new String[] {}, "triple braces '{{{...}}}' or amp");
		assertResolveArgs("triple braces '{{{...}}}' or amp", new String[] {"r1", "r2", "r3"}, "triple braces '{{{...}}}' or amp");
	}

	private void assertResolveArgs(String text, String[] args, String expected) {
		assertEquals(expected, Result.resolveArgs(text, args));
	}
}
