package interface_adapter.recipes;

import java.util.ArrayList;
import java.util.List;

/**
 * The View Model for the recipe results in the main area.
 */
public class RecipesViewModel {

    private final List<RecipesViewModelListener> listeners = new ArrayList<>();

    public static final String VIEW_NAME = "recipes";

    private RecipesState state = new RecipesState();

    public RecipesState getState() {
        return this.state;
    }

    public void setState(RecipesState state) {
        this.state = state;
    }

    public void addListener(RecipesViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (RecipesViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }

}
