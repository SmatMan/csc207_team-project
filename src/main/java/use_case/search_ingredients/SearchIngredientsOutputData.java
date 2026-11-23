package use_case.search_ingredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output Data for the Search Ingredients Use Case.
 */
public class SearchIngredientsOutputData {

    private final String query;
    private final List<IngredientSuggestion> suggestions;

    /**
     * Creates the output data.
     * @param query what the user typed
     * @param suggestions the matching ingredients, best first
     */
    public SearchIngredientsOutputData(String query, List<IngredientSuggestion> suggestions) {
        this.query = query;
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(suggestions));
    }

    public String getQuery() {
        return query;
    }

    public List<IngredientSuggestion> getSuggestions() {
        return suggestions;
    }
}
