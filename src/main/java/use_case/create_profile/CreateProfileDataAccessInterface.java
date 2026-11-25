package use_case.create_profile;

import entity.UserProfile;

/**
 * What the use case needs from storage. The interactor only sees this interface,
 * so it never knows whether profiles live in a file, a database or memory.
 */
public interface CreateProfileDataAccessInterface {

    /**
     * Saves the profile if its name is free. The name check and the save happen in one call,
     * so two saves can't both pass the check and then overwrite each other.
     * @param normalizedName the profile name, lowercased with {@link java.util.Locale#ROOT}
     * @param profile the profile to save
     * @return true if the profile was saved, false if the name was already taken
     * @throws ProfileStorageException if the save fails for a reason other than the name
     */
    boolean saveIfNameAvailable(String normalizedName, UserProfile profile) throws ProfileStorageException;
}
