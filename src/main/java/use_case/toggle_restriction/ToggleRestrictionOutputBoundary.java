package use_case.toggle_restriction;

import use_case.profile.ProfileOutputData;

/**
 * The output boundary for the Toggle Restriction Use Case.
 */
public interface ToggleRestrictionOutputBoundary {

    /**
     * Prepares the success view for the Toggle Restriction Use Case.
     * @param outputData the updated profile snapshot
     */
    void prepareSuccessView(ProfileOutputData outputData);

    /**
     * Prepares the failure view for the Toggle Restriction Use Case.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
