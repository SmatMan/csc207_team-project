package view.components;

import java.util.Locale;

/**
 * Marks the parts of a suggestion that match what the user typed, as HTML with
 * the matching letters in bold.
 */
public final class MatchHighlighter {

    private MatchHighlighter() {
    }

    /**
     * Highlights the query inside the text. Each typed word is bolded where it appears
     * in the text; if it doesn't appear as typed (a misspelling such as "brwn"), its
     * letters are bolded in order within a single word ("Br-o-wn").
     * @param text the suggestion, e.g. "Brown rice"
     * @param query what the user typed, e.g. "brwn ri"
     * @return the text as an HTML fragment (escaped, no surrounding html tag)
     */
    public static String toHtml(String text, String query) {
        final boolean[] bold = new boolean[text.length()];
        final String lowerText = text.toLowerCase(Locale.ROOT);
        if (query != null) {
            for (String word : query.toLowerCase(Locale.ROOT).trim().split("[^a-z0-9%]+")) {
                if (!word.isEmpty() && !markSubstring(lowerText, word, bold)) {
                    markLettersInOneWord(lowerText, word, bold);
                }
            }
        }
        return render(text, bold);
    }

    private static boolean markSubstring(String text, String word, boolean[] bold) {
        final int start = text.indexOf(word);
        if (start < 0) {
            return false;
        }
        for (int i = start; i < start + word.length(); i++) {
            bold[i] = true;
        }
        return true;
    }

    /**
     * Finds the first word of the text that contains the typed letters in order,
     * starting with the same letter, and marks those letters.
     */
    private static void markLettersInOneWord(String text, String letters, boolean[] bold) {
        int wordStart = 0;
        while (wordStart < text.length()) {
            int wordEnd = text.indexOf(' ', wordStart);
            if (wordEnd < 0) {
                wordEnd = text.length();
            }
            final int[] positions = subsequence(text, wordStart, wordEnd, letters);
            if (positions != null) {
                for (int position : positions) {
                    bold[position] = true;
                }
                return;
            }
            wordStart = wordEnd + 1;
        }
    }

    private static int[] subsequence(String text, int from, int to, String letters) {
        if (from >= to || text.charAt(from) != letters.charAt(0)) {
            return null;
        }
        final int[] positions = new int[letters.length()];
        int next = 0;
        for (int i = from; i < to && next < letters.length(); i++) {
            if (text.charAt(i) == letters.charAt(next)) {
                positions[next++] = i;
            }
        }
        return next == letters.length() ? positions : null;
    }

    private static String render(String text, boolean[] bold) {
        final StringBuilder html = new StringBuilder();
        boolean open = false;
        for (int i = 0; i < text.length(); i++) {
            if (bold[i] != open) {
                html.append(bold[i] ? "<b>" : "</b>");
                open = bold[i];
            }
            html.append(escape(text.charAt(i)));
        }
        if (open) {
            html.append("</b>");
        }
        return html.toString();
    }

    private static String escape(char c) {
        switch (c) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            default:
                return String.valueOf(c);
        }
    }
}
