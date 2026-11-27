package data_access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.FindRecipesDataAccessInterface;
import use_case.find_recipes.RecipeDetails;
import use_case.find_recipes.RecipeFilter;
import use_case.find_recipes.RecipeSearchException;
import use_case.find_recipes.RecipeSearchResult;
import use_case.view_recipe.ViewRecipeDataAccessInterface;

/**
 * In-memory implementation of the recipe DAO, for tests: pages through a fixed list of
 * recipes (or fails, if told to) and remembers how often it was asked. It ignores the
 * dish type and cuisine filter, and remembers the last one it was given. Recipes can be
 * given details for the View Recipe Use Case.
 */
public class InMemoryRecipeDataAccessObject implements FindRecipesDataAccessInterface,
                                                       ViewRecipeDataAccessInterface {

    private final List<Recipe> recipes;
    private final Map<Integer, RecipeDetails> details = new HashMap<>();
    private final boolean failing;
    private int searchCount;
    private RecipeFilter lastFilter;

    /**
     * Creates a DAO that pages through the given recipes.
     * @param recipes every recipe that matches any search
     */
    public InMemoryRecipeDataAccessObject(List<Recipe> recipes) {
        this(recipes, false);
    }

    private InMemoryRecipeDataAccessObject(List<Recipe> recipes, boolean failing) {
        this.recipes = new ArrayList<>(recipes);
        this.failing = failing;
    }

    /**
     * Creates a DAO whose searches always fail, as if the recipe service were unreachable.
     * @return the failing DAO
     */
    public static InMemoryRecipeDataAccessObject failing() {
        return new InMemoryRecipeDataAccessObject(new ArrayList<>(), true);
    }

    @Override
    public RecipeSearchResult findRecipes(List<Ingredient> ingredients, RecipeFilter filter, int offset, int count)
            throws RecipeSearchException {
        searchCount++;
        lastFilter = filter;
        if (failing) {
            throw new RecipeSearchException("Couldn't load recipes.", null);
        }
        final int from = Math.min(offset, recipes.size());
        final int to = Math.min(offset + count, recipes.size());
        return new RecipeSearchResult(recipes.subList(from, to), recipes.size(), details);
    }

    /**
     * Gives a recipe extra details, returned with it from now on.
     * @param recipeId the recipe's id
     * @param recipeDetails its details
     */
    public void putDetails(int recipeId, RecipeDetails recipeDetails) {
        details.put(recipeId, recipeDetails);
    }

    @Override
    public Recipe getRecipe(int recipeId) {
        for (Recipe recipe : recipes) {
            if (recipe.getId() == recipeId) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public RecipeDetails getDetails(int recipeId) {
        return details.get(recipeId);
    }

    public int getSearchCount() {
        return searchCount;
    }

    public RecipeFilter getLastFilter() {
        return lastFilter;
    }
}
