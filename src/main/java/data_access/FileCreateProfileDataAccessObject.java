package data_access;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;
import use_case.create_profile.CreateProfileDataAccessInterface;
import use_case.create_profile.ProfileStorageException;

/**
 * Saves named profiles to one JSON file. The file is an object keyed by the lowercased
 * profile name, and each value holds that profile's name and categories. A copy of the
 * same data stays in memory, so a name check never has to read the file.
 */
public class FileCreateProfileDataAccessObject implements CreateProfileDataAccessInterface {

    private final File profilesFile;
    private final Map<String, UserProfile> profiles = new LinkedHashMap<>();

    /**
     * Loads any profiles already saved at the given path.
     * @param profilesPath path of the file profiles are saved to
     * @throws RuntimeException if an existing file can't be read
     */
    public FileCreateProfileDataAccessObject(String profilesPath) {
        profilesFile = new File(profilesPath);
        // length() is 0 for a missing file and for an empty one, so both start with an empty map.
        if (profilesFile.length() > 0) {
            readProfiles();
        }
    }

    @Override
    public boolean saveIfNameAvailable(String normalizedName, UserProfile profile) throws ProfileStorageException {
        // Taken name: return false and leave the file alone.
        if (profiles.containsKey(normalizedName)) {
            return false;
        }
        // Add to memory first, then write the whole map to disk.
        profiles.put(normalizedName, profile);
        try {
            writeProfiles();
        }
        catch (IOException ex) {
            // The write failed, so undo the add. Memory and disk stay in sync.
            profiles.remove(normalizedName);
            throw new ProfileStorageException("Failed to save profile", ex);
        }
        return true;
    }

    // Reads the file into the map. Only the constructor calls this.
    private void readProfiles() {
        final JSONObject json;
        try {
            json = new JSONObject(Files.readString(profilesFile.toPath(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String normalizedName : json.keySet()) {
            profiles.put(normalizedName, toProfile(json.getJSONObject(normalizedName)));
        }
    }

    // Rewrites the whole file from the map. The 2 is the indent, so the file stays readable.
    private void writeProfiles() throws IOException {
        final JSONObject json = new JSONObject();
        for (Map.Entry<String, UserProfile> entry : profiles.entrySet()) {
            json.put(entry.getKey(), toJson(entry.getValue()));
        }
        Files.writeString(profilesFile.toPath(), json.toString(2), StandardCharsets.UTF_8);
    }

    // Entity to JSON. The shape is {"name": ..., "intolerances": [{"name": ..., "ingredients": [...]}]}.
    // toProfile below reads the same shape back.
    private static JSONObject toJson(UserProfile profile) {
        final JSONObject json = new JSONObject();
        json.put("name", profile.getName());
        final JSONArray intolerances = new JSONArray();
        for (Intolerance intolerance : profile.getIntolerances()) {
            final JSONObject category = new JSONObject();
            category.put("name", intolerance.getName());
            final JSONArray ingredients = new JSONArray();
            for (Ingredient ingredient : intolerance.getIngredients()) {
                ingredients.put(ingredient.getName());
            }
            category.put("ingredients", ingredients);
            intolerances.put(category);
        }
        json.put("intolerances", intolerances);
        return json;
    }

    // JSON back to entity. Excluded ingredients are not stored on their own. The interactor
    // excludes every ingredient in every category it saves, so rebuilding does the same.
    private static UserProfile toProfile(JSONObject json) {
        final List<Intolerance> intolerances = new ArrayList<>();
        final JSONArray intoleranceArray = json.getJSONArray("intolerances");
        for (int i = 0; i < intoleranceArray.length(); i++) {
            final JSONObject category = intoleranceArray.getJSONObject(i);
            final JSONArray ingredientArray = category.getJSONArray("ingredients");
            final List<Ingredient> ingredients = new ArrayList<>();
            for (int j = 0; j < ingredientArray.length(); j++) {
                ingredients.add(new Ingredient(ingredientArray.getString(j)));
            }
            intolerances.add(new Intolerance(category.getString("name"), ingredients));
        }
        final UserProfile profile = new UserProfile(json.getString("name"), intolerances);
        for (Intolerance intolerance : intolerances) {
            for (Ingredient ingredient : intolerance.getIngredients()) {
                profile.exclude(ingredient);
            }
        }
        return profile;
    }
}
