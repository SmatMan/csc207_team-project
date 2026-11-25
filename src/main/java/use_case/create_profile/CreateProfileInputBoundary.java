package use_case.create_profile;

/**
 * The door into the use case. The controller calls this, and the interactor is the
 * only class behind it.
 */
public interface CreateProfileInputBoundary {

    /**
     * Checks a submission and creates the profile if it passes.
     * @param inputData the submitted name and category selections
     */
    void execute(CreateProfileInputData inputData);
}
