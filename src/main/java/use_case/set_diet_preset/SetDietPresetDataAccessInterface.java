package use_case.set_diet_preset;

import java.util.List;

import entity.DietPreset;
import entity.UserProfile;

/**
 * DAO interface for the Set Diet Preset Use Case.
 */
public interface SetDietPresetDataAccessInterface {

    /**
     * Returns the user's profile.
     * @return the user's profile
     */
    UserProfile getProfile();

    /**
     * Returns every diet preset the user can choose from.
     * @return the presets
     */
    List<DietPreset> getPresets();

    /**
     * Returns the preset with the given name (case-insensitive).
     * @param name the preset name
     * @return the preset, or null if there is none with that name
     */
    DietPreset getPreset(String name);

    /**
     * Saves the user's profile.
     * @param profile the profile to save
     */
    void save(UserProfile profile);
}
