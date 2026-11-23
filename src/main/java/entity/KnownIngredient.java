package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ingredient the app knows about: its canonical name, the category it belongs to,
 * and other names that should resolve to it (e.g. "prawns" for Shrimp, or similar
 * items such as "basmati rice" for White rice).
 */
public class KnownIngredient {

    private final Ingredient ingredient;
    private final String category;
    private final List<String> aliases;

    /**
     * Creates a known ingredient.
     * @param ingredient the ingredient, under its canonical name
     * @param category the category, e.g. "Vegetables"
     * @param aliases other names that resolve to this ingredient
     * @throws IllegalArgumentException if the category is blank
     */
    public KnownIngredient(Ingredient ingredient, String category, List<String> aliases) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category of " + ingredient + " cannot be empty");
        }
        this.ingredient = ingredient;
        this.category = category.trim();
        this.aliases = Collections.unmodifiableList(new ArrayList<>(aliases));
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public String getName() {
        return ingredient.getName();
    }

    public String getCategory() {
        return category;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
