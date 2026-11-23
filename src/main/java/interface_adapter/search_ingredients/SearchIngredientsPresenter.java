package interface_adapter.search_ingredients;

import java.util.ArrayList;
import java.util.List;

import use_case.search_ingredients.IngredientSuggestion;
import use_case.search_ingredients.SearchIngredientsOutputBoundary;
import use_case.search_ingredients.SearchIngredientsOutputData;

/**
 * The Presenter for the Search Ingredients Use Case.
 */
public class SearchIngredientsPresenter implements SearchIngredientsOutputBoundary {

    private final SearchIngredientsViewModel searchIngredientsViewModel;

    public SearchIngredientsPresenter(SearchIngredientsViewModel searchIngredientsViewModel) {
        this.searchIngredientsViewModel = searchIngredientsViewModel;
    }

    @Override
    public void prepareSuccessView(SearchIngredientsOutputData response) {
        final List<SearchIngredientsState.Suggestion> suggestions = new ArrayList<>();
        for (IngredientSuggestion suggestion : response.getSuggestions()) {
            // Show the other name whenever that's what matched, so "brwn" -> Sugar explains itself (brown sugar).
            final boolean viaAlias = !suggestion.getMatchedTerm().equalsIgnoreCase(suggestion.getName());
            suggestions.add(new SearchIngredientsState.Suggestion(suggestion.getName(),
                    suggestion.getMatchedTerm(), viaAlias));
        }

        final SearchIngredientsState state = searchIngredientsViewModel.getState();
        state.setQuery(response.getQuery());
        state.setSuggestions(suggestions);
        state.setError(null);
        searchIngredientsViewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        final SearchIngredientsState state = searchIngredientsViewModel.getState();
        state.setSuggestions(new ArrayList<>());
        state.setError(error);
        searchIngredientsViewModel.notifyStateChanged();
    }
}
