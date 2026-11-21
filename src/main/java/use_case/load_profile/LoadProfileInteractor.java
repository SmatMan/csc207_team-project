package use_case.load_profile;

import use_case.profile.ProfileOutputData;

/**
 * The Load Profile Interactor.
 */
public class LoadProfileInteractor implements LoadProfileInputBoundary {
    private final LoadProfileDataAccessInterface profileDataAccessObject;
    private final LoadProfileOutputBoundary profilePresenter;

    public LoadProfileInteractor(LoadProfileDataAccessInterface profileDataAccessInterface,
                                 LoadProfileOutputBoundary loadProfileOutputBoundary) {
        this.profileDataAccessObject = profileDataAccessInterface;
        this.profilePresenter = loadProfileOutputBoundary;
    }

    @Override
    public void execute() {
        final ProfileOutputData outputData = new ProfileOutputData(
                profileDataAccessObject.getProfile(), profileDataAccessObject.getPresets());
        profilePresenter.prepareSuccessView(outputData);
    }
}
