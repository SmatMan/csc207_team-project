package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The user's dietary profile: which ingredients they can't or won't eat.
 *
 * <p>An ingredient is excluded if the user excluded it by hand, or if an active
 * diet preset excludes it and the user hasn't allowed it back by hand.
 * Manual choices are remembered separately from presets, so turning a preset
 * off only undoes what that preset did.</p>
 */
public class UserProfile {

    private final List<Intolerance> intolerances;
    private final Set<DietPreset> activePresets = new LinkedHashSet<>();
    private final Set<Ingredient> manualExcluded = new LinkedHashSet<>();
    // Ingredients the user allowed even though an active preset excludes them.
    private final Set<Ingredient> manualAllowed = new LinkedHashSet<>();

    /**
     * Creates a profile with nothing excluded.
     * @param intolerances the categories of ingredients the user can exclude from
     */
    public UserProfile(List<Intolerance> intolerances) {
        this.intolerances = Collections.unmodifiableList(new ArrayList<>(intolerances));
    }

    public List<Intolerance> getIntolerances() {
        return intolerances;
    }

    public Set<DietPreset> getActivePresets() {
        return Collections.unmodifiableSet(activePresets);
    }

    /**
     * Returns whether the given ingredient is excluded.
     * @param ingredient the ingredient to check
     * @return true if the user can't or won't eat it
     */
    public boolean isExcluded(Ingredient ingredient) {
        return manualExcluded.contains(ingredient)
                || isExcludedByPreset(ingredient) && !manualAllowed.contains(ingredient);
    }

    /**
     * Returns every excluded ingredient, in the order the categories list them.
     * @return the excluded ingredients
     */
    public List<Ingredient> getExcludedIngredients() {
        final List<Ingredient> excluded = new ArrayList<>();
        for (Intolerance intolerance : intolerances) {
            for (Ingredient ingredient : intolerance.getIngredients()) {
                if (isExcluded(ingredient) && !excluded.contains(ingredient)) {
                    excluded.add(ingredient);
                }
            }
        }
        return excluded;
    }

    /**
     * Excludes an ingredient by hand.
     * @param ingredient the ingredient to exclude
     * @throws IllegalArgumentException if no category contains the ingredient
     */
    public void exclude(Ingredient ingredient) {
        requireKnown(ingredient);
        manualAllowed.remove(ingredient);
        if (!isExcludedByPreset(ingredient)) {
            manualExcluded.add(ingredient);
        }
    }

    /**
     * Allows an ingredient by hand, overriding any active preset that excludes it.
     * @param ingredient the ingredient to allow
     * @throws IllegalArgumentException if no category contains the ingredient
     */
    public void allow(Ingredient ingredient) {
        requireKnown(ingredient);
        manualExcluded.remove(ingredient);
        if (isExcludedByPreset(ingredient)) {
            manualAllowed.add(ingredient);
        }
    }

    /**
     * Turns a diet preset on, excluding everything it excludes
     * (including anything previously allowed by hand).
     * @param preset the preset to turn on
     */
    public void applyPreset(DietPreset preset) {
        activePresets.add(preset);
        manualAllowed.removeAll(preset.getExcludes());
    }

    /**
     * Turns a diet preset off. Its ingredients become allowed again unless
     * another active preset excludes them or the user excluded them by hand.
     * @param preset the preset to turn off
     */
    public void removePreset(DietPreset preset) {
        activePresets.remove(preset);
        // Overrides only matter while some preset still excludes the ingredient.
        manualAllowed.removeIf(ingredient -> !isExcludedByPreset(ingredient));
    }

    /**
     * Returns whether a preset is active.
     * @param preset the preset to check
     * @return true if it is turned on
     */
    public boolean isPresetActive(DietPreset preset) {
        return activePresets.contains(preset);
    }

    /**
     * Returns whether an active preset has been changed by hand, i.e. the user
     * allowed back something it excludes.
     * @param preset the preset to check
     * @return true if the preset is active and at least one of its ingredients is allowed
     */
    public boolean isPresetModified(DietPreset preset) {
        if (!isPresetActive(preset)) {
            return false;
        }
        for (Ingredient ingredient : preset.getExcludes()) {
            if (manualAllowed.contains(ingredient)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludedByPreset(Ingredient ingredient) {
        for (DietPreset preset : activePresets) {
            if (preset.excludes(ingredient)) {
                return true;
            }
        }
        return false;
    }

    private void requireKnown(Ingredient ingredient) {
        for (Intolerance intolerance : intolerances) {
            if (intolerance.contains(ingredient)) {
                return;
            }
        }
        throw new IllegalArgumentException(ingredient + " is not in any intolerance category");
    }
}
