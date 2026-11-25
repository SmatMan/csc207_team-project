package interface_adapter.create_profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the current state for the Create Profile tab and a list of listeners.
 * When the presenter changes the state, it calls notifyStateChanged() and every
 * listener (the panel) refreshes. It has no Swing in it, so it stays in this layer.
 */
public class CreateProfileViewModel {

    public static final String VIEW_NAME = "create profile";

    private final List<CreateProfileViewModelListener> listeners = new ArrayList<>();

    private CreateProfileState state = new CreateProfileState();

    public CreateProfileState getState() {
        return this.state;
    }

    public void setState(CreateProfileState state) {
        this.state = state;
    }

    public void addListener(CreateProfileViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (CreateProfileViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }
}
