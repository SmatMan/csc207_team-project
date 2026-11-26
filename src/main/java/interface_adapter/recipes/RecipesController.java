package interface_adapter.recipes;

import java.util.List;

import use_case.find_recipes.FindRecipesInputBoundary;
import use_case.find_recipes.FindRecipesInputData;
import use_case.find_recipes.RecipeFilter;

/**
 * The controller for the Find Recipes Use Case.
 */
public class RecipesController {

    private final FindRecipesInputBoundary findRecipesUseCaseInteractor;

    public RecipesController(FindRecipesInputBoundary findRecipesUseCaseInteractor) {
        this.findRecipesUseCaseInteractor = findRecipesUseCaseInteractor;
    }

    /**
     * Executes the Find Recipes Use Case for the first page, with no category.
     * @param ingredientNames the ingredients in the pantry
     */
    public void findRecipes(List<String> ingredientNames) {
        findRecipes(ingredientNames, null, null);
    }

    /**
     * Executes the Find Recipes Use Case for the first page.
     * @param ingredientNames the ingredients in the pantry
     * @param dishType the dish type to narrow to (e.g. "main course"), or null for any
     * @param cuisine the cuisine to narrow to (e.g. "Italian"), or null for any
     */
    public void findRecipes(List<String> ingredientNames, String dishType, String cuisine) {
        findRecipesUseCaseInteractor.execute(
                new FindRecipesInputData(ingredientNames, new RecipeFilter(dishType, cuisine), 0));
    }

    /**
     * Executes the Find Recipes Use Case for the next page of recipes.
     * @param ingredientNames the ingredients in the pantry
     * @param dishType the dish type being shown, or null for any
     * @param cuisine the cuisine being shown, or null for any
     * @param alreadyShown how many recipes are already on screen
     */
    public void loadMore(List<String> ingredientNames, String dishType, String cuisine, int alreadyShown) {
        findRecipesUseCaseInteractor.execute(
                new FindRecipesInputData(ingredientNames, new RecipeFilter(dishType, cuisine), alreadyShown));
    }
}
