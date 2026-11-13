package interface_adapter.restrictions;

import java.util.ArrayList;
import java.util.List;

/**
 * The View Model for the Restrictions tab of the sidebar.
 */
public class RestrictionsViewModel {

    private final List<RestrictionsViewModelListener> listeners = new ArrayList<>();

    public static final String VIEW_NAME = "restrictions";

    private RestrictionsState state = new RestrictionsState();

    public RestrictionsState getState() {
        return this.state;
    }

    public void setState(RestrictionsState state) {
        this.state = state;
    }

    public void addListener(RestrictionsViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (RestrictionsViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }

}
