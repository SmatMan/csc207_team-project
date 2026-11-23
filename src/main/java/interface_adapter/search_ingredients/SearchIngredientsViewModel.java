package interface_adapter.search_ingredients;

import java.util.ArrayList;
import java.util.List;

/**
 * The View Model for the add-ingredient search.
 */
public class SearchIngredientsViewModel {

    private final List<SearchIngredientsViewModelListener> listeners = new ArrayList<>();

    public static final String VIEW_NAME = "search ingredients";

    private SearchIngredientsState state = new SearchIngredientsState();

    public SearchIngredientsState getState() {
        return this.state;
    }

    public void setState(SearchIngredientsState state) {
        this.state = state;
    }

    public void addListener(SearchIngredientsViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (SearchIngredientsViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }

}
