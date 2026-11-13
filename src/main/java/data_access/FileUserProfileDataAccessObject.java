package data_access;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.DietPreset;
import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;
import use_case.load_profile.LoadProfileDataAccessInterface;
import use_case.set_diet_preset.SetDietPresetDataAccessInterface;
import use_case.toggle_restriction.ToggleRestrictionDataAccessInterface;

/**
 * DAO for the user's dietary profile. The catalogue of categories and presets is read
 * from a JSON resource bundled with the app; the user's choices are saved to a JSON file.
 *
 * <p>NOTE: the bundled catalogue (restrictions.json) is a placeholder. Its categories and
 * ingredients will be expanded or trimmed to match the ingredient list produced by the
 * Spoonacular API calls (use case 2). Profiles saved against an older catalogue still load:
 * anything no longer in the catalogue is skipped.</p>
 */
public class FileUserProfileDataAccessObject implements LoadProfileDataAccessInterface,
                                                        ToggleRestrictionDataAccessInterface,
                                                        SetDietPresetDataAccessInterface {

    private static final String ACTIVE_PRESETS = "activePresets";
    private static final String MANUAL_EXCLUDED = "manualExcluded";
    private static final String MANUAL_ALLOWED = "manualAllowed";

    private final File profileFile;
    private final List<Intolerance> intolerances = new ArrayList<>();
    private final List<DietPreset> presets = new ArrayList<>();
    private UserProfile profile;

    /**
     * Construct this DAO from the bundled catalogue and the user's saved profile, if any.
     * @param catalogueResource classpath path of the catalogue, e.g. "/restrictions.json"
     * @param profilePath path of the file the user's profile is saved to
     * @throws RuntimeException if the catalogue is missing or invalid, or the profile can't be read
     */
    public FileUserProfileDataAccessObject(String catalogueResource, String profilePath) {
        readCatalogue(readResource(catalogueResource));

        profileFile = new File(profilePath);
        if (profileFile.length() == 0) {
            profile = new UserProfile(intolerances);
        }
        else {
            profile = readProfile();
        }
    }

    @Override
    public UserProfile getProfile() {
        return profile;
    }

    @Override
    public List<DietPreset> getPresets() {
        return Collections.unmodifiableList(presets);
    }

    @Override
    public DietPreset getPreset(String name) {
        for (DietPreset preset : presets) {
            if (preset.getName().equalsIgnoreCase(name)) {
                return preset;
            }
        }
        return null;
    }

    @Override
    public void save(UserProfile userProfile) {
        this.profile = userProfile;

        final JSONObject json = new JSONObject();
        json.put(ACTIVE_PRESETS, names(userProfile.getActivePresets()));
        json.put(MANUAL_EXCLUDED, names(userProfile.getManualExcluded()));
        json.put(MANUAL_ALLOWED, names(userProfile.getManualAllowed()));
        try {
            Files.writeString(profileFile.toPath(), json.toString(2), StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void readCatalogue(JSONObject catalogue) {
        final JSONArray intoleranceArray = catalogue.getJSONArray("intolerances");
        for (int i = 0; i < intoleranceArray.length(); i++) {
            final JSONObject intolerance = intoleranceArray.getJSONObject(i);
            intolerances.add(new Intolerance(intolerance.getString("name"),
                    new ArrayList<>(ingredients(intolerance.getJSONArray("ingredients")))));
        }

        // A preset may only exclude ingredients that appear in some category.
        final UserProfile catalogueCheck = new UserProfile(intolerances);
        final JSONArray presetArray = catalogue.getJSONArray("presets");
        for (int i = 0; i < presetArray.length(); i++) {
            final JSONObject preset = presetArray.getJSONObject(i);
            final Set<Ingredient> excludes = ingredients(preset.getJSONArray("excludes"));
            for (Ingredient ingredient : excludes) {
                if (!catalogueCheck.contains(ingredient)) {
                    throw new RuntimeException("Preset \"" + preset.getString("name")
                            + "\" excludes \"" + ingredient + "\", which is in no category");
                }
            }
            presets.add(new DietPreset(preset.getString("name"), excludes));
        }
    }

    private UserProfile readProfile() {
        final JSONObject json;
        try {
            json = new JSONObject(Files.readString(profileFile.toPath(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Skip anything no longer in the catalogue, e.g. after editing restrictions.json.
        final UserProfile empty = new UserProfile(intolerances);
        final Set<DietPreset> active = new LinkedHashSet<>();
        for (Object name : json.optJSONArray(ACTIVE_PRESETS, new JSONArray())) {
            final DietPreset preset = getPreset(name.toString());
            if (preset != null) {
                active.add(preset);
            }
        }
        final Set<Ingredient> excluded = knownIngredients(json.optJSONArray(MANUAL_EXCLUDED, new JSONArray()), empty);
        final Set<Ingredient> allowed = knownIngredients(json.optJSONArray(MANUAL_ALLOWED, new JSONArray()), empty);
        return new UserProfile(intolerances, active, excluded, allowed);
    }

    private static Set<Ingredient> knownIngredients(JSONArray names, UserProfile profile) {
        final Set<Ingredient> known = new LinkedHashSet<>();
        for (Ingredient ingredient : ingredients(names)) {
            if (profile.contains(ingredient)) {
                known.add(ingredient);
            }
        }
        return known;
    }

    private static Set<Ingredient> ingredients(JSONArray names) {
        final Set<Ingredient> ingredients = new LinkedHashSet<>();
        for (int i = 0; i < names.length(); i++) {
            ingredients.add(new Ingredient(names.getString(i)));
        }
        return ingredients;
    }

    private static JSONArray names(Set<?> items) {
        final JSONArray array = new JSONArray();
        for (Object item : items) {
            array.put(item.toString());
        }
        return array;
    }

    private static JSONObject readResource(String resource) {
        try (InputStream in = FileUserProfileDataAccessObject.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new RuntimeException("Missing resource: " + resource);
            }
            return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
