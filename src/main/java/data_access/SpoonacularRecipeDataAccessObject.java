package data_access;

import java.util.ArrayList;
import java.util.List;

import API.ApiCall;
import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.FindRecipesDataAccessInterface;
import use_case.find_recipes.RecipeSearchException;
import use_case.find_recipes.RecipeSearchResult;

/**
 * DAO that finds recipes through the Spoonacular API, using the existing
 * {@link ApiCall} (use case 2) unchanged.
 *
 * <p>ApiCall's findByIngredients search returns a fixed number of recipes and can't
 * page, so this only ever has a first page. Once a paged search (complexSearch) is
 * available, use the offset and count here.</p>
 */
public class SpoonacularRecipeDataAccessObject implements FindRecipesDataAccessInterface {

    @Override
    public RecipeSearchResult findRecipes(List<Ingredient> ingredients, int offset, int count)
            throws RecipeSearchException {
        if (offset > 0) {
            // No later pages: report that everything has already been shown.
            return new RecipeSearchResult(new ArrayList<>(), offset);
        }
        try {
            final List<Recipe> recipes = ApiCall.MakeAPICall(ingredients);
            return new RecipeSearchResult(recipes, recipes.size());
        }
        catch (RuntimeException ex) {
            // Network failures and unexpected replies (e.g. the daily limit) both surface here.
            throw new RecipeSearchException(
                    "Couldn't load recipes. Check your connection, or try again later.", ex);
        }
    }
}
