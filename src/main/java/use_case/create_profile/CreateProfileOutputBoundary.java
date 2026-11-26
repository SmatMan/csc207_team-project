package use_case.create_profile;

/**
 * Where the interactor sends its result. The presenter implements this, so the
 * interactor never knows what the screen looks like.
 */
public interface CreateProfileOutputBoundary {

    /**
     * Reports that the profile was created.
     * @param outputData the created profile's name
     */
    void prepareSuccessView(CreateProfileOutputData outputData);

    /**
     * Reports that the profile was not created.
     * @param message the reason, written so the user can read it directly
     */
    void prepareFailView(String message);
}
