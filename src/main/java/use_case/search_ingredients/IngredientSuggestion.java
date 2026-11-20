package use_case.search_ingredients;

/**
 * One ingredient suggested for a search query.
 */
public class IngredientSuggestion {

    private final String name;
    private final String category;
    private final String matchedTerm;
    private final MatchKind kind;

    /**
     * Creates a suggestion.
     * @param name the ingredient's canonical name, e.g. "White rice"
     * @param category the ingredient's category, e.g. "Grains"
     * @param matchedTerm the name or alias that matched, e.g. "basmati rice"
     * @param kind how the query matched
     */
    public IngredientSuggestion(String name, String category, String matchedTerm, MatchKind kind) {
        this.name = name;
        this.category = category;
        this.matchedTerm = matchedTerm;
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getMatchedTerm() {
        return matchedTerm;
    }

    public MatchKind getKind() {
        return kind;
    }
}
