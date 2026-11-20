package use_case.search_ingredients;

/**
 * Input Boundary for searching the known ingredients.
 */
public interface SearchIngredientsInputBoundary {

    /**
     * Executes the search ingredients use case.
     * @param searchIngredientsInputData the input data
     */
    void execute(SearchIngredientsInputData searchIngredientsInputData);
}
