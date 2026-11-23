package use_case.search_ingredients;

import java.util.ArrayList;
import java.util.List;

/**
 * The Search Ingredients Interactor.
 */
public class SearchIngredientsInteractor implements SearchIngredientsInputBoundary {
    private final SearchIngredientsDataAccessInterface ingredientDataAccessObject;
    private final SearchIngredientsOutputBoundary searchPresenter;
    private final IngredientMatcher matcher = new IngredientMatcher();

    public SearchIngredientsInteractor(SearchIngredientsDataAccessInterface ingredientDataAccessInterface,
                                       SearchIngredientsOutputBoundary searchIngredientsOutputBoundary) {
        this.ingredientDataAccessObject = ingredientDataAccessInterface;
        this.searchPresenter = searchIngredientsOutputBoundary;
    }

    @Override
    public void execute(SearchIngredientsInputData searchIngredientsInputData) {
        final String query = searchIngredientsInputData.getQuery();
        if (query == null || query.isBlank()) {
            // Nothing typed yet: not an error, just nothing to suggest.
            searchPresenter.prepareSuccessView(new SearchIngredientsOutputData("", new ArrayList<>()));
            return;
        }

        final List<IngredientSuggestion> suggestions =
                matcher.match(query, ingredientDataAccessObject.getKnownIngredients());
        if (suggestions.isEmpty()) {
            searchPresenter.prepareFailView("No ingredient matches \"" + query.trim() + "\".");
        }
        else {
            searchPresenter.prepareSuccessView(new SearchIngredientsOutputData(query, suggestions));
        }
    }
}
