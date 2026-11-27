package use_case.view_recipe;

import entity.Recipe;
import use_case.find_recipes.RecipeDetails;

/**
 * DAO interface for the View Recipe Use Case: the recipes found by recent searches.
 */
public interface ViewRecipeDataAccessInterface {

    /**
     * Returns a recipe found by a recent search.
     * @param recipeId the recipe's id
     * @return the recipe, or null if it isn't among the recipes found
     */
    Recipe getRecipe(int recipeId);

    /**
     * Returns the extra details the recipe service gave for a recipe.
     * @param recipeId the recipe's id
     * @return its details, or null if there are none
     */
    RecipeDetails getDetails(int recipeId);
}
