package interface_adapter.recipes;

import java.util.List;

import use_case.find_recipes.FindRecipesInputBoundary;
import use_case.find_recipes.FindRecipesInputData;

/**
 * The controller for the Find Recipes Use Case.
 */
public class RecipesController {

    private final FindRecipesInputBoundary findRecipesUseCaseInteractor;

    public RecipesController(FindRecipesInputBoundary findRecipesUseCaseInteractor) {
        this.findRecipesUseCaseInteractor = findRecipesUseCaseInteractor;
    }

    /**
     * Executes the Find Recipes Use Case.
     * @param ingredientNames the ingredients in the pantry
     */
    public void findRecipes(List<String> ingredientNames) {
        findRecipesUseCaseInteractor.execute(new FindRecipesInputData(ingredientNames));
    }

    /**
     * Executes the Find Recipes Use Case for the next page of recipes.
     * @param ingredientNames the ingredients in the pantry
     * @param alreadyShown how many recipes are already on screen
     */
    public void loadMore(List<String> ingredientNames, int alreadyShown) {
        findRecipesUseCaseInteractor.execute(new FindRecipesInputData(ingredientNames, alreadyShown));
    }
}
