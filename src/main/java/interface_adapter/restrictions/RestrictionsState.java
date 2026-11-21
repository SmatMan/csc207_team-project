package interface_adapter.restrictions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The state for the Restrictions View Model.
 */
public class RestrictionsState {
    private Map<String, List<String>> ingredientsByCategory = new LinkedHashMap<>();
    private Set<String> excludedIngredients = new LinkedHashSet<>();
    private List<String> presetNames = new ArrayList<>();
    private Set<String> activePresets = new LinkedHashSet<>();
    private Set<String> modifiedPresets = new LinkedHashSet<>();
    private String error;

    public Map<String, List<String>> getIngredientsByCategory() {
        return ingredientsByCategory;
    }

    public Set<String> getExcludedIngredients() {
        return excludedIngredients;
    }

    public List<String> getPresetNames() {
        return presetNames;
    }

    public Set<String> getActivePresets() {
        return activePresets;
    }

    public Set<String> getModifiedPresets() {
        return modifiedPresets;
    }

    public String getError() {
        return error;
    }

    public void setIngredientsByCategory(Map<String, List<String>> ingredientsByCategory) {
        this.ingredientsByCategory = ingredientsByCategory;
    }

    public void setExcludedIngredients(Set<String> excludedIngredients) {
        this.excludedIngredients = excludedIngredients;
    }

    public void setPresetNames(List<String> presetNames) {
        this.presetNames = presetNames;
    }

    public void setActivePresets(Set<String> activePresets) {
        this.activePresets = activePresets;
    }

    public void setModifiedPresets(Set<String> modifiedPresets) {
        this.modifiedPresets = modifiedPresets;
    }

    public void setError(String error) {
        this.error = error;
    }

}
