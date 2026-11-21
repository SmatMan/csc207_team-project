package use_case.set_diet_preset;

import entity.DietPreset;
import entity.UserProfile;
import use_case.profile.ProfileOutputData;

/**
 * The Set Diet Preset Interactor.
 */
public class SetDietPresetInteractor implements SetDietPresetInputBoundary {
    private final SetDietPresetDataAccessInterface profileDataAccessObject;
    private final SetDietPresetOutputBoundary profilePresenter;

    public SetDietPresetInteractor(SetDietPresetDataAccessInterface profileDataAccessInterface,
                                   SetDietPresetOutputBoundary setDietPresetOutputBoundary) {
        this.profileDataAccessObject = profileDataAccessInterface;
        this.profilePresenter = setDietPresetOutputBoundary;
    }

    @Override
    public void execute(SetDietPresetInputData setDietPresetInputData) {
        final String name = setDietPresetInputData.getPresetName();
        final DietPreset preset = profileDataAccessObject.getPreset(name);
        if (preset == null) {
            profilePresenter.prepareFailView("Unknown diet preset: \"" + name + "\".");
        }
        else {
            final UserProfile profile = profileDataAccessObject.getProfile();
            if (setDietPresetInputData.isActive()) {
                profile.applyPreset(preset);
            }
            else {
                profile.removePreset(preset);
            }
            profileDataAccessObject.save(profile);

            profilePresenter.prepareSuccessView(
                    new ProfileOutputData(profile, profileDataAccessObject.getPresets()));
        }
    }
}
