package use_case.search_ingredients;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import entity.KnownIngredient;

/**
 * Ranks known ingredients against a search query. Each ingredient is scored by its
 * best-matching name or alias, from exact matches down to misspellings and finally
 * ingredients that only share a word with the query.
 */
public class IngredientMatcher {

    /** The most suggestions returned for one query. */
    public static final int MAX_SUGGESTIONS = 6;

    // Suggestions scoring below this are too weak to show.
    private static final double MIN_SCORE = 0.45;
    // How similar (0 to 1) a misspelling must be to count as a typo ("chikn" -> chicken is 0.71).
    private static final double TYPO_THRESHOLD = 0.70;
    // Shared-word matches are a last resort, so only a few are shown.
    private static final int MAX_SIMILAR = 3;
    private static final int MIN_TYPO_LENGTH = 3;

    /**
     * Returns the best-matching ingredients for a query, best first.
     * @param query what the user typed
     * @param knownIngredients the ingredients to search
     * @return up to {@link #MAX_SUGGESTIONS} suggestions; empty if the query is blank or nothing matches
     */
    public List<IngredientSuggestion> match(String query, List<KnownIngredient> knownIngredients) {
        final String normalizedQuery = normalize(query);
        final List<Scored> results = new ArrayList<>();
        if (normalizedQuery.isEmpty()) {
            return new ArrayList<>();
        }

        for (KnownIngredient known : knownIngredients) {
            Scored best = score(normalizedQuery, known, known.getName(), false);
            for (String alias : known.getAliases()) {
                final Scored candidate = score(normalizedQuery, known, alias, true);
                // Strictly greater, so the canonical name wins ties.
                if (candidate.score > best.score) {
                    best = candidate;
                }
            }
            if (best.score >= MIN_SCORE) {
                results.add(best);
            }
        }

        results.sort(Comparator.comparingDouble((Scored s) -> -s.score)
                .thenComparingInt(s -> s.known.getName().length())
                .thenComparing(s -> s.known.getName()));

        // Shared-word matches are only a fallback: drop them if anything matched better.
        int limit = MAX_SUGGESTIONS;
        if (!results.isEmpty() && results.get(0).kind == MatchKind.SIMILAR) {
            limit = MAX_SIMILAR;
        }
        else {
            results.removeIf(result -> result.kind == MatchKind.SIMILAR);
        }

        final List<IngredientSuggestion> suggestions = new ArrayList<>();
        for (Scored result : results.subList(0, Math.min(limit, results.size()))) {
            suggestions.add(new IngredientSuggestion(result.known.getName(), result.known.getCategory(),
                    result.term, result.kind));
        }
        return suggestions;
    }

    private static Scored score(String query, KnownIngredient known, String term, boolean isAlias) {
        final String candidate = normalize(term);

        if (candidate.equals(query)) {
            return new Scored(known, term, isAlias ? MatchKind.ALIAS : MatchKind.EXACT, isAlias ? 0.97 : 1.0);
        }
        // Prefer short names the query nearly fills ("tomato" -> Tomatoes over Tomato sauce).
        final double fill = (double) query.length() / candidate.length();
        if (candidate.startsWith(query)) {
            return isAlias
                    ? new Scored(known, term, MatchKind.ALIAS, 0.70 + 0.05 * fill)
                    : new Scored(known, term, MatchKind.PREFIX, 0.85 + 0.10 * fill);
        }
        if (query.length() >= 2 && hasWordStartingWith(candidate, query)) {
            return isAlias
                    ? new Scored(known, term, MatchKind.ALIAS, 0.65)
                    : new Scored(known, term, MatchKind.PREFIX, 0.75);
        }

        final double typo = query.length() >= MIN_TYPO_LENGTH ? typoSimilarity(query, candidate) : 0;
        if (typo >= TYPO_THRESHOLD) {
            // A misspelt name beats an equally misspelt alias ("brwn" -> Brown rice over brown sugar).
            return new Scored(known, term, MatchKind.TYPO, (isAlias ? 0.42 : 0.45) + 0.35 * typo);
        }

        final double overlap = wordOverlap(query, candidate);
        final double base = isAlias ? 0.37 : 0.40;
        return new Scored(known, term, MatchKind.SIMILAR, overlap > 0 ? base + 0.20 * overlap : 0);
    }

    /**
     * Similarity of the query to the whole candidate, or to the start of it,
     * so that half-typed misspellings ("brwn") still find "brown rice".
     */
    private static double typoSimilarity(String query, String candidate) {
        double best = similarity(query, candidate);
        if (query.length() > MIN_TYPO_LENGTH) {
            for (int length = query.length() - 1; length <= query.length() + 1; length++) {
                if (length < candidate.length()) {
                    best = Math.max(best, similarity(query, candidate.substring(0, length)));
                }
            }
        }
        return best;
    }

    /**
     * How much of the query's text is made of words that also appear (or nearly appear)
     * in the candidate. Weighted by word length, so "red quinoa" leans on "quinoa", not "red".
     */
    private static double wordOverlap(String query, String candidate) {
        final String[] candidateWords = candidate.split(" ");
        int matchedLetters = 0;
        int totalLetters = 0;
        for (String queryWord : significantWords(query)) {
            totalLetters += queryWord.length();
            for (String candidateWord : candidateWords) {
                if (candidateWord.equals(queryWord)
                        || queryWord.length() >= 4 && candidateWord.startsWith(queryWord)
                        || queryWord.length() >= 4 && similarity(queryWord, candidateWord) >= 0.8) {
                    matchedLetters += queryWord.length();
                    break;
                }
            }
        }
        return totalLetters == 0 ? 0 : (double) matchedLetters / totalLetters;
    }

    private static List<String> significantWords(String text) {
        final List<String> words = new ArrayList<>();
        for (String word : text.split(" ")) {
            if (word.length() >= MIN_TYPO_LENGTH) {
                words.add(word);
            }
        }
        return words;
    }

    private static boolean hasWordStartingWith(String candidate, String query) {
        for (String word : candidate.split(" ")) {
            if (word.startsWith(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1 for identical strings, falling towards 0 as the edit distance grows.
     */
    private static double similarity(String a, String b) {
        final int longest = Math.max(a.length(), b.length());
        return longest == 0 ? 1 : 1.0 - (double) editDistance(a, b) / longest;
    }

    /**
     * Levenshtein distance: the fewest single-character edits turning one string into the other.
     */
    private static int editDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                final int substitution = previous[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(substitution, Math.min(previous[j] + 1, current[j - 1] + 1));
            }
            final int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    /**
     * Lowercases and reduces punctuation and repeated spaces to single spaces.
     */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9%]+", " ").trim();
    }

    /**
     * A known ingredient with the term that matched best and its score.
     */
    private static final class Scored {
        private final KnownIngredient known;
        private final String term;
        private final MatchKind kind;
        private final double score;

        private Scored(KnownIngredient known, String term, MatchKind kind, double score) {
            this.known = known;
            this.term = term;
            this.kind = kind;
            this.score = score;
        }
    }
}
