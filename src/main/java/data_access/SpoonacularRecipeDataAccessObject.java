package data_access;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import API.ApiCall;
import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.FindRecipesDataAccessInterface;
import use_case.find_recipes.RecipeDetails;
import use_case.find_recipes.RecipeFilter;
import use_case.find_recipes.RecipeSearchException;
import use_case.find_recipes.RecipeSearchResult;
import use_case.view_recipe.ViewRecipeDataAccessInterface;

/**
 * DAO that finds recipes through the Spoonacular API, using the existing
 * {@link ApiCall} (use case 2) unchanged. It remembers the recipes it found, so opening
 * one doesn't call the API again.
 *
 * <p>ApiCall's findByIngredients search returns a fixed number of recipes and can't
 * page or filter, so this only ever has a first page, ignores the dish type and cuisine,
 * and has no details. Once complexSearch is available, pass the offset, count and filter
 * (its "type" and "cuisine" parameters) here, and the extra fields (score, health score,
 * time, servings, summary, steps, ingredient quantities, with addRecipeInformation,
 * addRecipeInstructions and fillIngredients) as {@link RecipeDetails}.</p>
 */
public class SpoonacularRecipeDataAccessObject implements FindRecipesDataAccessInterface,
                                                          ViewRecipeDataAccessInterface {

    // Searches run on a background thread and recipes are opened on the Swing thread.
    private final Map<Integer, Recipe> found = new ConcurrentHashMap<>();

    @Override
    public RecipeSearchResult findRecipes(List<Ingredient> ingredients, RecipeFilter filter, int offset, int count)
            throws RecipeSearchException {
        if (offset > 0) {
            // No later pages: report that everything has already been shown.
            return new RecipeSearchResult(new ArrayList<>(), offset);
        }
        try {
            final List<Recipe> recipes = ApiCall.MakeAPICall(ingredients);
            for (Recipe recipe : recipes) {
                found.put(recipe.getId(), recipe);
            }
            return new RecipeSearchResult(recipes, recipes.size());
        }
        catch (RuntimeException ex) {
            // Network failures and unexpected replies (e.g. the daily limit) both surface here.
            throw new RecipeSearchException(
                    "Couldn't load recipes. Check your connection, or try again later.", ex);
        }
    }

    @Override
    public Recipe getRecipe(int recipeId) {
        return found.get(recipeId);
    }

    @Override
    public RecipeDetails getDetails(int recipeId) {
        // findByIngredients gives no details yet.
        return null;
    }
}
