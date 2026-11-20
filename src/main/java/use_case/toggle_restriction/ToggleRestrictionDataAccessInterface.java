package use_case.toggle_restriction;

import java.util.List;

import entity.DietPreset;
import entity.UserProfile;

/**
 * DAO interface for the Toggle Restriction Use Case.
 */
public interface ToggleRestrictionDataAccessInterface {

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
     * Saves the user's profile.
     * @param profile the profile to save
     */
    void save(UserProfile profile);
}
