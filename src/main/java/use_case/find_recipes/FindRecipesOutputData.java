package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output Data for the Find Recipes Use Case: one page of recipes.
 */
public class FindRecipesOutputData {

    private final int ingredientCount;
    private final List<FoundRecipe> recipes;
    private final int offset;
    private final boolean moreAvailable;

    /**
     * Creates the output data for the first page, with nothing more to load.
     * @param ingredientCount how many pantry ingredients the search used
     * @param recipes the recipes found, fewest missing ingredients first
     */
    public FindRecipesOutputData(int ingredientCount, List<FoundRecipe> recipes) {
        this(ingredientCount, recipes, 0, false);
    }

    /**
     * Creates the output data for a page of recipes.
     * @param ingredientCount how many pantry ingredients the search used
     * @param recipes the recipes on this page, fewest missing ingredients first
     * @param offset how many recipes came before this page (0 for the first page)
     * @param moreAvailable whether another page can be loaded after this one
     */
    public FindRecipesOutputData(int ingredientCount, List<FoundRecipe> recipes, int offset, boolean moreAvailable) {
        this.ingredientCount = ingredientCount;
        this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
        this.offset = offset;
        this.moreAvailable = moreAvailable;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    public List<FoundRecipe> getRecipes() {
        return recipes;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isMoreAvailable() {
        return moreAvailable;
    }
}
