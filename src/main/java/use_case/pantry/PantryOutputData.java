package use_case.pantry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.Ingredient;
import entity.Pantry;

/**
 * Output Data shared by the pantry use cases: a snapshot of the pantry as plain
 * strings, so the presenter never touches entities.
 */
public class PantryOutputData {

    private final List<String> ingredientNames = new ArrayList<>();

    /**
     * Takes a snapshot of the given pantry.
     * @param pantry the user's pantry
     */
    public PantryOutputData(Pantry pantry) {
        for (Ingredient ingredient : pantry.getIngredients()) {
            ingredientNames.add(ingredient.getName());
        }
    }

    public List<String> getIngredientNames() {
        return Collections.unmodifiableList(ingredientNames);
    }
}
