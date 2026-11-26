package use_case.create_profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything the view submitted: the profile name and the selections made for it.
 * It carries raw strings and does no checking. The interactor does that.
 */
public class CreateProfileInputData {

    private final String profileName;
    private final List<IntoleranceInputData> intolerances;

    /**
     * Packages up one profile-creation request.
     * @param profileName the name the user typed
     * @param intolerances the categories selected, each with its selected ingredients
     */
    public CreateProfileInputData(String profileName, List<IntoleranceInputData> intolerances) {
        this.profileName = profileName;
        // Copy and lock the list so the view can't change it after submitting.
        this.intolerances = Collections.unmodifiableList(new ArrayList<>(intolerances));
    }

    public String getProfileName() {
        return profileName;
    }

    public List<IntoleranceInputData> getIntolerances() {
        return intolerances;
    }
}
