package interface_adapter.pantry;

import java.util.ArrayList;
import java.util.List;

/**
 * The View Model for the Ingredients tab of the sidebar.
 */
public class PantryViewModel {

    private final List<PantryViewModelListener> listeners = new ArrayList<>();

    public static final String VIEW_NAME = "pantry";

    private PantryState state = new PantryState();

    public PantryState getState() {
        return this.state;
    }

    public void setState(PantryState state) {
        this.state = state;
    }

    public void addListener(PantryViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (PantryViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }

}
