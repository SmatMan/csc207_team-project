package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A category of foods a user may need to avoid, such as "Dairy" or "Meat",
 * and the ingredients in it. Which of these ingredients are excluded is
 * decided by the {@link UserProfile}.
 */
public class Intolerance {

    private final String name;
    private final List<Ingredient> ingredients;

    /**
     * Creates an intolerance category.
     * @param name the category name, e.g. "Dairy"
     * @param ingredients the ingredients in this category, in display order
     * @throws IllegalArgumentException if the name is blank or there are no ingredients
     */
    public Intolerance(String name, List<Ingredient> ingredients) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Intolerance name cannot be empty");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Intolerance \"" + name + "\" needs at least one ingredient");
        }
        this.name = name.trim();
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Returns whether the given ingredient belongs to this category.
     * @param ingredient the ingredient to look for
     * @return true if it is in this category
     */
    public boolean contains(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }
}
