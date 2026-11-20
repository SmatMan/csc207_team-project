package interface_adapter.restrictions;

import use_case.load_profile.LoadProfileInputBoundary;
import use_case.set_diet_preset.SetDietPresetInputBoundary;
import use_case.set_diet_preset.SetDietPresetInputData;
import use_case.toggle_restriction.ToggleRestrictionInputBoundary;
import use_case.toggle_restriction.ToggleRestrictionInputData;

/**
 * The controller for the Restrictions tab: loading the profile,
 * toggling single ingredients and turning diet presets on or off.
 */
public class RestrictionsController {

    private final LoadProfileInputBoundary loadProfileUseCaseInteractor;
    private final ToggleRestrictionInputBoundary toggleRestrictionUseCaseInteractor;
    private final SetDietPresetInputBoundary setDietPresetUseCaseInteractor;

    public RestrictionsController(LoadProfileInputBoundary loadProfileUseCaseInteractor,
                                  ToggleRestrictionInputBoundary toggleRestrictionUseCaseInteractor,
                                  SetDietPresetInputBoundary setDietPresetUseCaseInteractor) {
        this.loadProfileUseCaseInteractor = loadProfileUseCaseInteractor;
        this.toggleRestrictionUseCaseInteractor = toggleRestrictionUseCaseInteractor;
        this.setDietPresetUseCaseInteractor = setDietPresetUseCaseInteractor;
    }

    /**
     * Executes the Load Profile Use Case.
     */
    public void loadProfile() {
        loadProfileUseCaseInteractor.execute();
    }

    /**
     * Executes the Toggle Restriction Use Case.
     * @param ingredientName the ingredient to change
     * @param excluded true to exclude it, false to allow it
     */
    public void setExcluded(String ingredientName, boolean excluded) {
        toggleRestrictionUseCaseInteractor.execute(new ToggleRestrictionInputData(ingredientName, excluded));
    }

    /**
     * Executes the Set Diet Preset Use Case.
     * @param presetName the preset to change
     * @param active true to turn it on, false to turn it off
     */
    public void setPresetActive(String presetName, boolean active) {
        setDietPresetUseCaseInteractor.execute(new SetDietPresetInputData(presetName, active));
    }
}
