package view.components;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prepares recipe descriptions (HTML from the recipe service) for Swing, which only
 * understands simple HTML: keeps bold, italics, line breaks and web links, drops anything
 * else, and can shorten a description to its first sentences.
 */
public final class RecipeHtml {

    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z0-9]+)([^>]*)>");
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*\"(https?://[^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private RecipeHtml() {
    }

    /**
     * Keeps only bold, italics, paragraphs, line breaks and web links, without other attributes.
     * @param html the description from the recipe service
     * @return HTML Swing can show safely (no surrounding html tag)
     */
    public static String clean(String html) {
        if (html == null) {
            return "";
        }
        final String withoutScripts = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "");
        final Matcher tag = TAG.matcher(withoutScripts);
        final StringBuilder out = new StringBuilder();
        int last = 0;
        while (tag.find()) {
            out.append(withoutScripts, last, tag.start());
            out.append(keep(tag.group(1), tag.group(2).toLowerCase(Locale.ROOT), tag.group(3)));
            last = tag.end();
        }
        out.append(withoutScripts.substring(last));
        return out.toString().trim();
    }

    /**
     * The first sentences of a description, with any tags left open closed again.
     * @param html the description (cleaned or not)
     * @param sentences how many sentences to keep
     * @return the shortened, cleaned HTML
     */
    public static String firstSentences(String html, int sentences) {
        final String cleaned = clean(html);
        int found = 0;
        boolean inTag = false;
        int cut = cleaned.length();
        for (int i = 0; i < cleaned.length(); i++) {
            final char c = cleaned.charAt(i);
            if (c == '<') {
                inTag = true;
            }
            else if (c == '>') {
                inTag = false;
            }
            else if (!inTag && (c == '.' || c == '!' || c == '?')
                    && (i + 1 == cleaned.length() || cleaned.charAt(i + 1) == ' ')) {
                found++;
                if (found == sentences) {
                    cut = i + 1;
                    break;
                }
            }
        }
        return closeOpenTags(cleaned.substring(0, cut));
    }

    /**
     * Escapes plain text so it shows literally inside HTML.
     * @param text plain text, e.g. a recipe step
     * @return the escaped text
     */
    public static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String keep(String closing, String name, String attributes) {
        final String swingName;
        switch (name) {
            case "b":
            case "strong":
                swingName = "b";
                break;
            case "i":
            case "em":
                swingName = "i";
                break;
            case "br":
                return closing.isEmpty() ? "<br>" : "";
            case "p":
                swingName = "p";
                break;
            case "a":
                if (!closing.isEmpty()) {
                    return "</a>";
                }
                final Matcher href = HREF.matcher(attributes);
                return href.find() ? "<a href=\"" + href.group(1) + "\">" : "<a>";
            default:
                return "";
        }
        return "<" + closing + swingName + ">";
    }

    private static String closeOpenTags(String html) {
        final Deque<String> open = new ArrayDeque<>();
        final Matcher tag = TAG.matcher(html);
        while (tag.find()) {
            final String name = tag.group(2).toLowerCase(Locale.ROOT);
            if ("br".equals(name)) {
                continue;
            }
            if (tag.group(1).isEmpty()) {
                open.push(name);
            }
            else if (!open.isEmpty() && open.peek().equals(name)) {
                open.pop();
            }
        }
        final StringBuilder closed = new StringBuilder(html);
        while (!open.isEmpty()) {
            closed.append("</").append(open.pop()).append('>');
        }
        return closed.toString();
    }
}
