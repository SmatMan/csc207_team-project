package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Recipe;

/**
 * One page of recipes from the recipe service, how many recipes match in total, and
 * any extra details the service provided for them (matched by recipe id).
 */
public class RecipeSearchResult {

    private final List<Recipe> recipes;
    private final int totalResults;
    private final Map<Integer, RecipeDetails> detailsById;

    /**
     * Creates a page of results without details.
     * @param recipes the recipes on this page
     * @param totalResults how many recipes match across all pages
     */
    public RecipeSearchResult(List<Recipe> recipes, int totalResults) {
        this(recipes, totalResults, new HashMap<>());
    }

    /**
     * Creates a page of results with details.
     * @param recipes the recipes on this page
     * @param totalResults how many recipes match across all pages
     * @param detailsById extra details for some or all of the recipes, keyed by recipe id
     */
    public RecipeSearchResult(List<Recipe> recipes, int totalResults, Map<Integer, RecipeDetails> detailsById) {
        this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
        this.totalResults = totalResults;
        this.detailsById = Collections.unmodifiableMap(new HashMap<>(detailsById));
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Returns the extra details for a recipe.
     * @param recipeId the recipe's id
     * @return its details, or null if the service gave none
     */
    public RecipeDetails getDetails(int recipeId) {
        return detailsById.get(recipeId);
    }
}
