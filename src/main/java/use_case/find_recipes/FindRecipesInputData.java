package use_case.find_recipes;

import java.util.ArrayList;
import java.util.List;

/**
 * The Input Data for the Find Recipes Use Case.
 */
public class FindRecipesInputData {

    private final List<String> ingredientNames;
    private final RecipeFilter filter;
    private final int offset;

    /**
     * Asks for the first page of recipes, with no filter.
     * @param ingredientNames the ingredients in the pantry
     */
    public FindRecipesInputData(List<String> ingredientNames) {
        this(ingredientNames, RecipeFilter.NONE, 0);
    }

    /**
     * Asks for a later page of recipes, with no filter.
     * @param ingredientNames the ingredients in the pantry
     * @param offset how many recipes have already been shown
     */
    public FindRecipesInputData(List<String> ingredientNames, int offset) {
        this(ingredientNames, RecipeFilter.NONE, offset);
    }

    /**
     * Asks for a page of recipes of a given dish type and/or cuisine.
     * @param ingredientNames the ingredients in the pantry
     * @param filter the dish type and cuisine to narrow to
     * @param offset how many recipes have already been shown (0 for the first page)
     */
    public FindRecipesInputData(List<String> ingredientNames, RecipeFilter filter, int offset) {
        this.ingredientNames = new ArrayList<>(ingredientNames);
        this.filter = filter == null ? RecipeFilter.NONE : filter;
        this.offset = offset;
    }

    List<String> getIngredientNames() {
        return ingredientNames;
    }

    RecipeFilter getFilter() {
        return filter;
    }

    int getOffset() {
        return offset;
    }

}
