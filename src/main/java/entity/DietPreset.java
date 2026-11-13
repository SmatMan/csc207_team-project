package entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A named diet, such as "Vegan" or "Pescatarian", that excludes a set of ingredients.
 * Presets are identified by name (case-insensitive).
 */
public class DietPreset {

    private final String name;
    private final Set<Ingredient> excludes;

    /**
     * Creates a diet preset.
     * @param name the preset name, e.g. "Vegan"
     * @param excludes the ingredients this diet excludes
     * @throws IllegalArgumentException if the name is blank or nothing is excluded
     */
    public DietPreset(String name, Set<Ingredient> excludes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Diet preset name cannot be empty");
        }
        if (excludes == null || excludes.isEmpty()) {
            throw new IllegalArgumentException("Diet preset \"" + name + "\" must exclude something");
        }
        this.name = name.trim();
        this.excludes = Collections.unmodifiableSet(new LinkedHashSet<>(excludes));
    }

    public String getName() {
        return name;
    }

    public Set<Ingredient> getExcludes() {
        return excludes;
    }

    /**
     * Returns whether this diet excludes the given ingredient.
     * @param ingredient the ingredient to check
     * @return true if this diet excludes it
     */
    public boolean excludes(Ingredient ingredient) {
        return excludes.contains(ingredient);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DietPreset)) {
            return false;
        }
        return name.toLowerCase(Locale.ROOT).equals(((DietPreset) other).name.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
