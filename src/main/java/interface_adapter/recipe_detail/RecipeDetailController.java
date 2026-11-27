package interface_adapter.recipe_detail;

import interface_adapter.ViewManagerModel;
import interface_adapter.recipes.RecipesViewModel;
import use_case.view_recipe.ViewRecipeInputBoundary;
import use_case.view_recipe.ViewRecipeInputData;

/**
 * The controller for the recipe screen: opening a recipe, and going back to the recipe grid.
 */
public class RecipeDetailController {

    private final ViewRecipeInputBoundary viewRecipeUseCaseInteractor;
    private final ViewManagerModel viewManagerModel;

    public RecipeDetailController(ViewRecipeInputBoundary viewRecipeUseCaseInteractor,
                                  ViewManagerModel viewManagerModel) {
        this.viewRecipeUseCaseInteractor = viewRecipeUseCaseInteractor;
        this.viewManagerModel = viewManagerModel;
    }

    /**
     * Executes the View Recipe Use Case.
     * @param recipeId the recipe to open
     */
    public void open(int recipeId) {
        viewRecipeUseCaseInteractor.execute(new ViewRecipeInputData(recipeId));
    }

    /**
     * Goes back from the recipe screen to the recipe grid.
     */
    public void back() {
        viewManagerModel.setState(RecipesViewModel.VIEW_NAME);
        viewManagerModel.notifyViewChanged();
    }
}
