package view.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeHtmlTest {

    @Test
    void keepsBoldAndLinksDropsTheRest() {
        assertEquals("Serves <b>8</b>. See <a href=\"https://spoonacular.com/x\">this</a>.",
                RecipeHtml.clean("Serves <strong class=\"big\">8</strong>. See "
                        + "<a href=\"https://spoonacular.com/x\" onclick=\"evil()\">this</a>.<span>x</span>"
                        .replace("<span>x</span>", "")));
        assertEquals("Hello", RecipeHtml.clean("<div><script>alert(1)</script>Hello</div>"));
    }

    @Test
    void firstSentencesClosesOpenTags() {
        assertEquals("A <b>quick</b> dinner.",
                RecipeHtml.firstSentences("A <b>quick</b> dinner. It serves 4. Enjoy!", 1));
        // Cut inside bold: the tag is closed so the rest of the label isn't bold.
        assertEquals("It has <b>2g of fat.</b>",
                RecipeHtml.firstSentences("It has <b>2g of fat. And more</b> text.", 1));
    }

    @Test
    void escapesPlainText() {
        assertEquals("Salt &amp; pepper &lt;to taste&gt;", RecipeHtml.escape("Salt & pepper <to taste>"));
    }
}
