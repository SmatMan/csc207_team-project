package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A named profile. It holds the categories the user picked and tracks which of
 * their ingredients are excluded right now.
 */
public class UserProfile {

    private final String name;
    private final List<Intolerance> intolerances;
    // Starts empty on purpose. Building a profile excludes nothing. Something has to call exclude().
    private final Set<Ingredient> excluded = new LinkedHashSet<>();

    /**
     * Builds a profile with nothing excluded yet.
     * @param name the profile's display name
     * @param intolerances the categories the user can exclude from
     * @throws IllegalArgumentException if the name is blank
     */
    public UserProfile(String name, List<Intolerance> intolerances) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        this.name = name.trim();
        // Same defensive copy as Intolerance: copy it, then lock it.
        this.intolerances = Collections.unmodifiableList(new ArrayList<>(intolerances));
    }

    public String getName() {
        return name;
    }

    public List<Intolerance> getIntolerances() {
        return intolerances;
    }

    /**
     * Checks whether any category in this profile has the given ingredient.
     * @param ingredient the ingredient to look for
     * @return true if some category contains it
     */
    public boolean contains(Ingredient ingredient) {
        for (Intolerance intolerance : intolerances) {
            if (intolerance.contains(ingredient)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the user excluded the given ingredient.
     * @param ingredient the ingredient to check
     * @return true if it was excluded
     */
    public boolean isExcluded(Ingredient ingredient) {
        return excluded.contains(ingredient);
    }

    /**
     * Excludes an ingredient. It has to belong to one of this profile's categories first.
     * @param ingredient the ingredient to exclude
     * @throws IllegalArgumentException if no category contains the ingredient
     */
    public void exclude(Ingredient ingredient) {
        // You can't exclude something the profile never offered.
        if (!contains(ingredient)) {
            throw new IllegalArgumentException(ingredient + " is not in any intolerance category");
        }
        excluded.add(ingredient);
    }
}
