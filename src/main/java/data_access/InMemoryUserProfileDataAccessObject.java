package data_access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.DietPreset;
import entity.Intolerance;
import entity.UserProfile;
import use_case.load_profile.LoadProfileDataAccessInterface;
import use_case.set_diet_preset.SetDietPresetDataAccessInterface;
import use_case.toggle_restriction.ToggleRestrictionDataAccessInterface;

/**
 * In-memory implementation of the DAO for the user's dietary profile. This implementation
 * does NOT persist data between runs of the program.
 */
public class InMemoryUserProfileDataAccessObject implements LoadProfileDataAccessInterface,
                                                            ToggleRestrictionDataAccessInterface,
                                                            SetDietPresetDataAccessInterface {

    private final List<DietPreset> presets;
    private UserProfile profile;

    public InMemoryUserProfileDataAccessObject(List<Intolerance> intolerances, List<DietPreset> presets) {
        this.presets = Collections.unmodifiableList(new ArrayList<>(presets));
        this.profile = new UserProfile(intolerances);
    }

    @Override
    public UserProfile getProfile() {
        return profile;
    }

    @Override
    public List<DietPreset> getPresets() {
        return presets;
    }

    @Override
    public DietPreset getPreset(String name) {
        for (DietPreset preset : presets) {
            if (preset.getName().equalsIgnoreCase(name)) {
                return preset;
            }
        }
        return null;
    }

    @Override
    public void save(UserProfile userProfile) {
        this.profile = userProfile;
    }
}
