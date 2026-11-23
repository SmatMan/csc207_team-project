package view.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchHighlighterTest {

    @Test
    void boldsTheTypedPrefixIgnoringCase() {
        assertEquals("<b>Tom</b>atoes", MatchHighlighter.toHtml("Tomatoes", "tom"));
    }

    @Test
    void boldsEachTypedWord() {
        assertEquals("<b>Bro</b>wn <b>ri</b>ce", MatchHighlighter.toHtml("Brown rice", "bro ri"));
    }

    @Test
    void boldsMisspeltLettersInOrder() {
        assertEquals("<b>Br</b>o<b>wn</b> <b>rice</b>", MatchHighlighter.toHtml("Brown rice", "brwn rice"));
        assertEquals("<b>Chi</b>c<b>k</b>e<b>n</b>", MatchHighlighter.toHtml("Chicken", "chikn"));
    }

    @Test
    void leavesTextPlainWhenNothingMatches() {
        assertEquals("Salmon", MatchHighlighter.toHtml("Salmon", "xyz"));
        assertEquals("Salmon", MatchHighlighter.toHtml("Salmon", ""));
        assertEquals("Salmon", MatchHighlighter.toHtml("Salmon", null));
    }

    @Test
    void escapesHtml() {
        assertEquals("Salt &amp; <b>pepper</b>", MatchHighlighter.toHtml("Salt & pepper", "pepper"));
    }
}
