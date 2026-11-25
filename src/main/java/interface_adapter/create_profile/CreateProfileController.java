package interface_adapter.create_profile;

import java.util.List;

import entity.Intolerance;
import use_case.create_profile.CreateProfileInputBoundary;
import use_case.create_profile.CreateProfileInputData;
import use_case.create_profile.IntoleranceCatalogue;
import use_case.create_profile.IntoleranceInputData;

/**
 * The only thing the view talks to for this use case. It does two jobs: list the
 * categories for the checklist, and pass a submission on to the use case. The view
 * never touches the catalogue or the interactor directly.
 */
public class CreateProfileController {

    private final CreateProfileInputBoundary createProfileUseCaseInteractor;
    private final IntoleranceCatalogue catalogue;

    public CreateProfileController(CreateProfileInputBoundary createProfileUseCaseInteractor,
                                    IntoleranceCatalogue catalogue) {
        this.createProfileUseCaseInteractor = createProfileUseCaseInteractor;
        this.catalogue = catalogue;
    }

    /**
     * Returns the categories for the view to show as checkboxes.
     * @return the available intolerance categories
     */
    public List<Intolerance> getAvailableIntolerances() {
        return catalogue.getAvailableIntolerances();
    }

    /**
     * Wraps the submission in an input data object and runs the use case.
     * @param profileName the submitted profile name
     * @param intolerances the submitted category selections
     */
    public void createProfile(String profileName, List<IntoleranceInputData> intolerances) {
        createProfileUseCaseInteractor.execute(new CreateProfileInputData(profileName, intolerances));
    }
}
