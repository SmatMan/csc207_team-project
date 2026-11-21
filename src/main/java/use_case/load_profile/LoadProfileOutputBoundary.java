package use_case.load_profile;

import use_case.profile.ProfileOutputData;

/**
 * The output boundary for the Load Profile Use Case.
 */
public interface LoadProfileOutputBoundary {

    /**
     * Prepares the view showing the loaded profile.
     * @param outputData the profile snapshot
     */
    void prepareSuccessView(ProfileOutputData outputData);
}
