package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.Recipe;

/**
 * One page of recipes from the recipe service, and how many recipes match in total.
 */
public class RecipeSearchResult {

    private final List<Recipe> recipes;
    private final int totalResults;

    /**
     * Creates a page of results.
     * @param recipes the recipes on this page
     * @param totalResults how many recipes match across all pages
     */
    public RecipeSearchResult(List<Recipe> recipes, int totalResults) {
        this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
        this.totalResults = totalResults;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public int getTotalResults() {
        return totalResults;
    }
}
