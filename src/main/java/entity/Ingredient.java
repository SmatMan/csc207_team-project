package entity;

import java.util.Locale;

/**
 * A food ingredient, identified by its name. Names are compared
 * case-insensitively, so "Shrimp" and "shrimp" are the same ingredient.
 */
public class Ingredient {

    private final String name;

    /**
     * Creates an ingredient with the given non-empty name.
     * @param name the ingredient name
     * @throws IllegalArgumentException if the name is null or blank
     */
    public Ingredient(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Ingredient)) {
            return false;
        }
        return key().equals(((Ingredient) other).key());
    }

    @Override
    public int hashCode() {
        return key().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    private String key() {
        return name.toLowerCase(Locale.ROOT);
    }
}
