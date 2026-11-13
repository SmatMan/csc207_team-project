package use_case.load_profile;

import java.util.List;

import entity.DietPreset;
import entity.UserProfile;

/**
 * DAO interface for the Load Profile Use Case.
 */
public interface LoadProfileDataAccessInterface {

    /**
     * Returns the user's saved profile, or a new empty one if none was saved.
     * @return the user's profile
     */
    UserProfile getProfile();

    /**
     * Returns every diet preset the user can choose from.
     * @return the presets
     */
    List<DietPreset> getPresets();
}
