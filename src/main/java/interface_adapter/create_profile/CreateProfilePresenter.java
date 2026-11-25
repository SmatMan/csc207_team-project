package interface_adapter.create_profile;

import use_case.create_profile.CreateProfileOutputBoundary;
import use_case.create_profile.CreateProfileOutputData;

/**
 * Turns the interactor's result into state the view can read. Each method updates the
 * state, then tells the view model to notify its listeners so the screen refreshes.
 */
public class CreateProfilePresenter implements CreateProfileOutputBoundary {

    private final CreateProfileViewModel createProfileViewModel;

    public CreateProfilePresenter(CreateProfileViewModel createProfileViewModel) {
        this.createProfileViewModel = createProfileViewModel;
    }

    @Override
    public void prepareSuccessView(CreateProfileOutputData outputData) {
        final CreateProfileState state = createProfileViewModel.getState();
        state.setCreatedProfileName(outputData.getProfileName());
        // Clear any old error so the view shows the success message.
        state.setError(null);
        createProfileViewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String message) {
        final CreateProfileState state = createProfileViewModel.getState();
        state.setError(message);
        createProfileViewModel.notifyStateChanged();
    }
}
