package interface_adapter.recipe_detail;

import java.util.ArrayList;
import java.util.List;

/**
 * The View Model for the recipe screen (one recipe in full).
 */
public class RecipeDetailViewModel {

    private final List<RecipeDetailViewModelListener> listeners = new ArrayList<>();

    public static final String VIEW_NAME = "recipe detail";

    private RecipeDetailState state = new RecipeDetailState();

    public RecipeDetailState getState() {
        return this.state;
    }

    public void setState(RecipeDetailState state) {
        this.state = state;
    }

    public void addListener(RecipeDetailViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (RecipeDetailViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }

}
