package use_case.create_profile;

/**
 * Thrown when a save fails for any reason except a taken name (a disk error, say).
 * It is a checked exception on purpose. Every caller has to decide what the user
 * sees when storage breaks, so a failed save can't crash the app.
 */
public class ProfileStorageException extends Exception {

    public ProfileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
