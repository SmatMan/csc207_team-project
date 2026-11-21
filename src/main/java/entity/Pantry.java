package entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The ingredients the user has at home. Each ingredient appears at most once
 * (compared case-insensitively, as {@link Ingredient} does), in the order it was added.
 */
public class Pantry {

    private final Set<Ingredient> ingredients = new LinkedHashSet<>();

    /**
     * Creates an empty pantry.
     */
    public Pantry() {
    }

    /**
     * Recreates a saved pantry. Duplicates are kept only once.
     * @param ingredients the ingredients in the pantry
     */
    public Pantry(Collection<Ingredient> ingredients) {
        this.ingredients.addAll(ingredients);
    }

    /**
     * Returns every ingredient in the pantry, in the order they were added.
     * @return the ingredients
     */
    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    /**
     * Returns whether the pantry holds the given ingredient.
     * @param ingredient the ingredient to look for
     * @return true if it is in the pantry
     */
    public boolean contains(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }

    /**
     * Adds an ingredient, unless the pantry already holds it.
     * @param ingredient the ingredient to add
     * @return true if it was added, false if it was already there
     */
    public boolean add(Ingredient ingredient) {
        return ingredients.add(ingredient);
    }

    /**
     * Removes an ingredient.
     * @param ingredient the ingredient to remove
     * @return true if it was removed, false if it wasn't in the pantry
     */
    public boolean remove(Ingredient ingredient) {
        return ingredients.remove(ingredient);
    }
}
