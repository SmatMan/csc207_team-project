package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;
import use_case.create_profile.ProfileStorageException;

class FileCreateProfileDataAccessObjectTest {

    // JUnit gives each test a fresh temp folder and deletes it afterward,
    // so these tests never touch the real profiles.json.
    @TempDir
    Path tempDir;

    // A free name saves and returns true.
    @Test
    void savesANewProfile() throws ProfileStorageException {
        final FileCreateProfileDataAccessObject dataAccess =
                new FileCreateProfileDataAccessObject(tempDir.resolve("profiles.json").toString());
        final UserProfile profile = new UserProfile("Home",
                List.of(new Intolerance("Dairy", List.of(new Ingredient("Milk")))));

        assertTrue(dataAccess.saveIfNameAvailable("home", profile));
    }

    // The second save under the same name returns false.
    @Test
    void rejectsATakenName() throws ProfileStorageException {
        final FileCreateProfileDataAccessObject dataAccess =
                new FileCreateProfileDataAccessObject(tempDir.resolve("profiles.json").toString());
        dataAccess.saveIfNameAvailable("home", new UserProfile("Home", List.of()));

        assertFalse(dataAccess.saveIfNameAvailable("home", new UserProfile("Home", List.of())));
    }

    // Save with one DAO, then open a second DAO on the same file. The second one
    // still sees the name as taken, and the file contains the ingredient. That
    // proves the data reached the disk.
    @Test
    void savedProfilesAreReloadedFromDisk() throws ProfileStorageException, IOException {
        final String path = tempDir.resolve("profiles.json").toString();
        final UserProfile profile = new UserProfile("Home",
                List.of(new Intolerance("Dairy", List.of(new Ingredient("Milk")))));
        profile.exclude(new Ingredient("Milk"));
        new FileCreateProfileDataAccessObject(path).saveIfNameAvailable("home", profile);

        final FileCreateProfileDataAccessObject reloaded = new FileCreateProfileDataAccessObject(path);

        assertFalse(reloaded.saveIfNameAvailable("home", new UserProfile("Home", List.of())));
        assertTrue(Files.readString(Path.of(path)).contains("Milk"));
    }
}
