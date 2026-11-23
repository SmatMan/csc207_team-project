package use_case.find_recipes;

import java.util.ArrayList;
import java.util.List;

/**
 * The Input Data for the Find Recipes Use Case.
 */
public class FindRecipesInputData {

    private final List<String> ingredientNames;
    private final int offset;

    /**
     * Asks for the first page of recipes.
     * @param ingredientNames the ingredients in the pantry
     */
    public FindRecipesInputData(List<String> ingredientNames) {
        this(ingredientNames, 0);
    }

    /**
     * Asks for a later page of recipes.
     * @param ingredientNames the ingredients in the pantry
     * @param offset how many recipes have already been shown
     */
    public FindRecipesInputData(List<String> ingredientNames, int offset) {
        this.ingredientNames = new ArrayList<>(ingredientNames);
        this.offset = offset;
    }

    List<String> getIngredientNames() {
        return ingredientNames;
    }

    int getOffset() {
        return offset;
    }

}
