package interface_adapter.create_profile;

/**
 * What the Create Profile view reads: the name of the profile just created, and an
 * error message. The view checks the error first, so a set error wins over an
 * older success.
 */
public class CreateProfileState {

    private String createdProfileName;
    private String error;

    public String getCreatedProfileName() {
        return createdProfileName;
    }

    public void setCreatedProfileName(String createdProfileName) {
        this.createdProfileName = createdProfileName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
