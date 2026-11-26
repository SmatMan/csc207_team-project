package use_case.create_profile;

/**
 * What a successful creation hands back: the profile's display name.
 * The presenter uses it to build the confirmation message.
 */
public class CreateProfileOutputData {

    private final String profileName;

    public CreateProfileOutputData(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}
