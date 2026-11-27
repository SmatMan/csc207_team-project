package use_case.view_recipe;

import java.util.ArrayList;
import java.util.List;

import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;

/**
 * The View Recipe Interactor. Opens a recipe found by a recent search, splitting its
 * ingredients into the ones the user has and the ones they'd need to buy.
 */
public class ViewRecipeInteractor implements ViewRecipeInputBoundary {
    private final ViewRecipeDataAccessInterface recipeDataAccessObject;
    private final ViewRecipeOutputBoundary recipePresenter;

    public ViewRecipeInteractor(ViewRecipeDataAccessInterface recipeDataAccessInterface,
                                ViewRecipeOutputBoundary viewRecipeOutputBoundary) {
        this.recipeDataAccessObject = recipeDataAccessInterface;
        this.recipePresenter = viewRecipeOutputBoundary;
    }

    @Override
    public void execute(ViewRecipeInputData viewRecipeInputData) {
        final int id = viewRecipeInputData.getRecipeId();
        final Recipe recipe = recipeDataAccessObject.getRecipe(id);
        if (recipe == null) {
            recipePresenter.prepareFailView("That recipe isn't available any more. Try searching again.");
            return;
        }

        RecipeDetails details = recipeDataAccessObject.getDetails(id);
        if (details == null) {
            details = RecipeDetails.builder().build();
        }
        // Quantities come from the details when the service gave them; otherwise just the names.
        final List<IngredientLine> have = details.getUsedLines().isEmpty()
                ? namesOnly(recipe.getUsedIngredients()) : details.getUsedLines();
        final List<IngredientLine> need = details.getMissedLines().isEmpty()
                ? namesOnly(recipe.getMissingIngredients()) : details.getMissedLines();

        recipePresenter.prepareSuccessView(new ViewRecipeOutputData(
                recipe.getId(), recipe.getName(), recipe.getImage(), details, have, need));
    }

    private static List<IngredientLine> namesOnly(List<Ingredient> ingredients) {
        final List<IngredientLine> lines = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            lines.add(new IngredientLine(ingredient.getName(), null, "", ""));
        }
        return lines;
    }
}
