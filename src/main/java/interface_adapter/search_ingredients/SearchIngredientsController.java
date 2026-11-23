package interface_adapter.search_ingredients;

import use_case.search_ingredients.SearchIngredientsInputBoundary;
import use_case.search_ingredients.SearchIngredientsInputData;

/**
 * The controller for the Search Ingredients Use Case.
 */
public class SearchIngredientsController {

    private final SearchIngredientsInputBoundary searchIngredientsUseCaseInteractor;

    public SearchIngredientsController(SearchIngredientsInputBoundary searchIngredientsUseCaseInteractor) {
        this.searchIngredientsUseCaseInteractor = searchIngredientsUseCaseInteractor;
    }

    /**
     * Executes the Search Ingredients Use Case.
     * @param query what the user has typed so far
     */
    public void search(String query) {
        searchIngredientsUseCaseInteractor.execute(new SearchIngredientsInputData(query));
    }
}
