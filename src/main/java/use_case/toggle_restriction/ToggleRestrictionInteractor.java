package use_case.toggle_restriction;

import entity.Ingredient;
import entity.UserProfile;
import use_case.profile.ProfileOutputData;

/**
 * The Toggle Restriction Interactor.
 */
public class ToggleRestrictionInteractor implements ToggleRestrictionInputBoundary {
    private final ToggleRestrictionDataAccessInterface profileDataAccessObject;
    private final ToggleRestrictionOutputBoundary profilePresenter;

    public ToggleRestrictionInteractor(ToggleRestrictionDataAccessInterface profileDataAccessInterface,
                                       ToggleRestrictionOutputBoundary toggleRestrictionOutputBoundary) {
        this.profileDataAccessObject = profileDataAccessInterface;
        this.profilePresenter = toggleRestrictionOutputBoundary;
    }

    @Override
    public void execute(ToggleRestrictionInputData toggleRestrictionInputData) {
        final String name = toggleRestrictionInputData.getIngredientName();
        if (name == null || name.isBlank()) {
            profilePresenter.prepareFailView("No ingredient was chosen.");
            return;
        }

        final UserProfile profile = profileDataAccessObject.getProfile();
        final Ingredient ingredient = new Ingredient(name);
        if (!profile.contains(ingredient)) {
            profilePresenter.prepareFailView("\"" + name + "\" is not in any restriction category.");
        }
        else {
            if (toggleRestrictionInputData.isExcluded()) {
                profile.exclude(ingredient);
            }
            else {
                profile.allow(ingredient);
            }
            profileDataAccessObject.save(profile);

            profilePresenter.prepareSuccessView(
                    new ProfileOutputData(profile, profileDataAccessObject.getPresets()));
        }
    }
}
