package use_case.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.DietPreset;
import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;

/**
 * Output Data shared by the dietary profile use cases: a snapshot of the whole
 * profile as plain strings, so the presenter never touches entities.
 */
public class ProfileOutputData {

    private final Map<String, List<String>> ingredientsByCategory = new LinkedHashMap<>();
    private final Set<String> excludedIngredients = new LinkedHashSet<>();
    private final List<String> presetNames = new ArrayList<>();
    private final Set<String> activePresets = new LinkedHashSet<>();
    private final Set<String> modifiedPresets = new LinkedHashSet<>();

    /**
     * Takes a snapshot of the given profile.
     * @param profile the user's profile
     * @param presets every diet preset the user can choose from
     */
    public ProfileOutputData(UserProfile profile, List<DietPreset> presets) {
        for (Intolerance intolerance : profile.getIntolerances()) {
            final List<String> names = new ArrayList<>();
            for (Ingredient ingredient : intolerance.getIngredients()) {
                names.add(ingredient.getName());
                if (profile.isExcluded(ingredient)) {
                    excludedIngredients.add(ingredient.getName());
                }
            }
            ingredientsByCategory.put(intolerance.getName(), Collections.unmodifiableList(names));
        }
        for (DietPreset preset : presets) {
            presetNames.add(preset.getName());
            if (profile.isPresetActive(preset)) {
                activePresets.add(preset.getName());
            }
            if (profile.isPresetModified(preset)) {
                modifiedPresets.add(preset.getName());
            }
        }
    }

    public Map<String, List<String>> getIngredientsByCategory() {
        return Collections.unmodifiableMap(ingredientsByCategory);
    }

    public Set<String> getExcludedIngredients() {
        return Collections.unmodifiableSet(excludedIngredients);
    }

    public List<String> getPresetNames() {
        return Collections.unmodifiableList(presetNames);
    }

    public Set<String> getActivePresets() {
        return Collections.unmodifiableSet(activePresets);
    }

    public Set<String> getModifiedPresets() {
        return Collections.unmodifiableSet(modifiedPresets);
    }
}
