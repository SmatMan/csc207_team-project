package use_case.set_diet_preset;

import use_case.profile.ProfileOutputData;

/**
 * The output boundary for the Set Diet Preset Use Case.
 */
public interface SetDietPresetOutputBoundary {

    /**
     * Prepares the success view for the Set Diet Preset Use Case.
     * @param outputData the updated profile snapshot
     */
    void prepareSuccessView(ProfileOutputData outputData);

    /**
     * Prepares the failure view for the Set Diet Preset Use Case.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
