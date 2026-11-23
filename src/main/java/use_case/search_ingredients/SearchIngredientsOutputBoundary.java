package use_case.search_ingredients;

/**
 * The output boundary for the Search Ingredients Use Case.
 */
public interface SearchIngredientsOutputBoundary {

    /**
     * Prepares the view listing the suggestions (empty if nothing has been typed yet).
     * @param outputData the query and its suggestions
     */
    void prepareSuccessView(SearchIngredientsOutputData outputData);

    /**
     * Prepares the view for a query that matched nothing.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
