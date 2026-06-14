package com.fortify.ssc.parser.fortifyaa.util;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Converts Markdown (as produced by the agentic analyzer) into HTML for display
 * in SSC. SSC sanitizes issue-detail fields with a Jsoup whitelist on
 * serialization, so the HTML produced here is further filtered before it reaches
 * the browser:
 * <ul>
 *   <li>custom attributes use {@code Safelist.relaxed()} (pre, code, tables,
 *       headings, lists, links, ...);</li>
 *   <li>the built-in {@code brief}/{@code detail}/{@code recommendation} fields
 *       use the more restrictive basic whitelist.</li>
 * </ul>
 */
public final class MarkdownUtil {
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    private MarkdownUtil() {}

    /**
     * Render the given Markdown string to HTML. Returns {@code null} for blank
     * input. Plain (non-Markdown) text is rendered as a single paragraph, which
     * still produces valid HTML so SSC treats and displays it consistently.
     */
    public static String toHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return null;
        }
        Node document = PARSER.parse(markdown);
        return StringUtils.trimToNull(RENDERER.render(document));
    }
}
