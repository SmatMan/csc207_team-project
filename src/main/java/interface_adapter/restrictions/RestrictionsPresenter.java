package interface_adapter.restrictions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import use_case.load_profile.LoadProfileOutputBoundary;
import use_case.profile.ProfileOutputData;
import use_case.set_diet_preset.SetDietPresetOutputBoundary;
import use_case.toggle_restriction.ToggleRestrictionOutputBoundary;

/**
 * The Presenter for the dietary profile use cases (load profile, toggle restriction
 * and set diet preset). They all report the same profile snapshot, which is shown
 * in the Restrictions tab.
 */
public class RestrictionsPresenter implements LoadProfileOutputBoundary,
                                              ToggleRestrictionOutputBoundary,
                                              SetDietPresetOutputBoundary {

    private final RestrictionsViewModel restrictionsViewModel;

    public RestrictionsPresenter(RestrictionsViewModel restrictionsViewModel) {
        this.restrictionsViewModel = restrictionsViewModel;
    }

    @Override
    public void prepareSuccessView(ProfileOutputData response) {
        final RestrictionsState state = restrictionsViewModel.getState();
        state.setIngredientsByCategory(new LinkedHashMap<>(response.getIngredientsByCategory()));
        state.setExcludedIngredients(new LinkedHashSet<>(response.getExcludedIngredients()));
        state.setPresetNames(new ArrayList<>(response.getPresetNames()));
        state.setActivePresets(new LinkedHashSet<>(response.getActivePresets()));
        state.setModifiedPresets(new LinkedHashSet<>(response.getModifiedPresets()));
        state.setError(null);
        restrictionsViewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        final RestrictionsState state = restrictionsViewModel.getState();
        state.setError(error);
        restrictionsViewModel.notifyStateChanged();
    }
}
