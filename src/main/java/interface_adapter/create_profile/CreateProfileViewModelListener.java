package interface_adapter.create_profile;

/**
 * Implemented by anything that wants to hear when the Create Profile state changes.
 * The panel is the one listener today.
 */
public interface CreateProfileViewModelListener {

    /**
     * Called after the state changes. Read the view model's state to see what changed.
     */
    void stateChanged();
}
