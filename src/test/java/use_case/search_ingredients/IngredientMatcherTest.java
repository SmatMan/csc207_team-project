package use_case.search_ingredients;

import java.util.List;

import data_access.FileIngredientCorpusDataAccessObject;
import entity.KnownIngredient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the ranking rules against the real bundled corpus, using the kinds of
 * things a user would actually type.
 */
class IngredientMatcherTest {

    private final List<KnownIngredient> corpus =
            new FileIngredientCorpusDataAccessObject("/ingredients.json").getKnownIngredients();
    private final IngredientMatcher matcher = new IngredientMatcher();

    private IngredientSuggestion top(String query) {
        final List<IngredientSuggestion> suggestions = matcher.match(query, corpus);
        assertFalse(suggestions.isEmpty(), "expected a match for \"" + query + "\"");
        return suggestions.get(0);
    }

    @Test
    void exactNameIgnoresCaseAndSpacing() {
        final IngredientSuggestion top = top("  EGGS ");
        assertEquals("Eggs", top.getName());
        assertEquals(MatchKind.EXACT, top.getKind());
    }

    @Test
    void misspellingFindsTheIngredient() {
        final IngredientSuggestion top = top("brwn rice");
        assertEquals("Brown rice", top.getName());
        assertEquals(MatchKind.TYPO, top.getKind());
    }

    @Test
    void halfTypedMisspellingFindsTheIngredient() {
        assertEquals("Brown rice", top("brwn").getName());
    }

    @Test
    void twoEditMisspellingFindsTheIngredient() {
        assertEquals("Chicken", top("chikn").getName());
    }

    @Test
    void sharedWordMatchesOnlyAppearAsAFallback() {
        // Brown rice is a close typo, so loose matches like "rice noodles" are left out.
        final List<IngredientSuggestion> suggestions = matcher.match("brwn rice", corpus);
        for (IngredientSuggestion suggestion : suggestions) {
            assertNotEquals(MatchKind.SIMILAR, suggestion.getKind());
        }
        // Nothing closer than a shared word, so a few similar items are offered.
        final List<IngredientSuggestion> similar = matcher.match("red quinoa", corpus);
        assertTrue(similar.size() <= 3);
    }

    @Test
    void similarItemResolvesThroughAlias() {
        final IngredientSuggestion top = top("basmati rice");
        assertEquals("White rice", top.getName());
        assertEquals(MatchKind.ALIAS, top.getKind());
        assertEquals("basmati rice", top.getMatchedTerm());
    }

    @Test
    void otherNameResolvesThroughAlias() {
        assertEquals("Shrimp", top("prawns").getName());
        assertEquals("Zucchini", top("courgette").getName());
    }

    @Test
    void prefixPrefersTheShortestFit() {
        final IngredientSuggestion top = top("tom");
        assertEquals("Tomatoes", top.getName());
        assertEquals(MatchKind.PREFIX, top.getKind());
    }

    @Test
    void unknownVarietyFallsBackToSharedWord() {
        final IngredientSuggestion top = top("red quinoa");
        assertEquals("Quinoa", top.getName());
        assertEquals(MatchKind.SIMILAR, top.getKind());
    }

    @Test
    void suggestionsAreCappedAndBestFirst() {
        final List<IngredientSuggestion> suggestions = matcher.match("rice", corpus);
        assertTrue(suggestions.size() <= IngredientMatcher.MAX_SUGGESTIONS);
        assertEquals("White rice", suggestions.get(0).getName());
        assertEquals("Brown rice", suggestions.get(1).getName());
    }

    @Test
    void gibberishAndBlankMatchNothing() {
        assertTrue(matcher.match("xqzzv", corpus).isEmpty());
        assertTrue(matcher.match("   ", corpus).isEmpty());
        assertTrue(matcher.match(null, corpus).isEmpty());
    }
}
