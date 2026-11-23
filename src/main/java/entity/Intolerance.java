package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A category of foods the user wants to avoid, like "Dairy", plus the specific
 * ingredients that sit under it. Once built, it never changes.
 */
public class Intolerance {

    private final String name;
    private final List<Ingredient> ingredients;

    /**
     * Builds a category. Every check runs up front, so a bad category never exists.
     * @param name the category name, e.g. "Dairy"
     * @param ingredients the excluded ingredients, in display order
     * @throws IllegalArgumentException if the name is blank, there are no ingredients,
     *         or the same ingredient appears more than once
     */
    public Intolerance(String name, List<Ingredient> ingredients) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Intolerance name cannot be empty");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Intolerance \"" + name + "\" needs at least one ingredient");
        }
        // A set drops repeats. If it comes out smaller than the list, something showed up twice.
        // The set uses Ingredient's equals, which ignores case, so "Milk" and "milk" collide.
        if (new LinkedHashSet<>(ingredients).size() != ingredients.size()) {
            throw new IllegalArgumentException("Intolerance \"" + name + "\" has a duplicate ingredient");
        }
        this.name = name.trim();
        // Copy the list, then lock the copy. Nobody outside can change what we hold.
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Checks whether the given ingredient sits under this category.
     * @param ingredient the ingredient to look for
     * @return true if it is in this category
     */
    public boolean contains(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }
}
