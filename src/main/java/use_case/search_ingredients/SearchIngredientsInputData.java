package use_case.search_ingredients;

/**
 * The Input Data for the Search Ingredients Use Case.
 */
public class SearchIngredientsInputData {

    private final String query;

    public SearchIngredientsInputData(String query) {
        this.query = query;
    }

    String getQuery() {
        return query;
    }

}
