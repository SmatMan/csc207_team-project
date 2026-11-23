package interface_adapter.search_ingredients;

import java.util.ArrayList;
import java.util.List;

/**
 * The state for the Search Ingredients View Model.
 */
public class SearchIngredientsState {
    private String query = "";
    private List<Suggestion> suggestions = new ArrayList<>();
    private String error;

    public String getQuery() {
        return query;
    }

    /**
     * Returns the suggestions, best match first.
     * @return the suggestions
     */
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public String getError() {
        return error;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * One suggested ingredient, as the view shows it.
     */
    public static class Suggestion {
        private final String name;
        private final String matchedTerm;
        private final boolean viaAlias;

        /**
         * Creates a suggestion.
         * @param name the ingredient's name, e.g. "White rice"
         * @param matchedTerm what the query matched, e.g. "basmati rice"
         * @param viaAlias true if the query matched another name rather than this name
         */
        public Suggestion(String name, String matchedTerm, boolean viaAlias) {
            this.name = name;
            this.matchedTerm = matchedTerm;
            this.viaAlias = viaAlias;
        }

        public String getName() {
            return name;
        }

        public String getMatchedTerm() {
            return matchedTerm;
        }

        public boolean isViaAlias() {
            return viaAlias;
        }
    }
}
