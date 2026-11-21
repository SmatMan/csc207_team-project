package use_case.remove_ingredient;

import entity.Ingredient;
import entity.Pantry;
import use_case.pantry.PantryOutputData;

/**
 * The Remove Ingredient Interactor.
 */
public class RemoveIngredientInteractor implements RemoveIngredientInputBoundary {
    private final RemoveIngredientDataAccessInterface pantryDataAccessObject;
    private final RemoveIngredientOutputBoundary pantryPresenter;

    public RemoveIngredientInteractor(RemoveIngredientDataAccessInterface pantryDataAccessInterface,
                                      RemoveIngredientOutputBoundary removeIngredientOutputBoundary) {
        this.pantryDataAccessObject = pantryDataAccessInterface;
        this.pantryPresenter = removeIngredientOutputBoundary;
    }

    @Override
    public void execute(RemoveIngredientInputData removeIngredientInputData) {
        final String name = removeIngredientInputData.getIngredientName();
        if (name == null || name.isBlank()) {
            pantryPresenter.prepareFailView("No ingredient was chosen.");
            return;
        }

        final Pantry pantry = pantryDataAccessObject.getPantry();
        final Ingredient ingredient = new Ingredient(name);
        if (!pantry.remove(ingredient)) {
            pantryPresenter.prepareFailView("\"" + ingredient.getName() + "\" is not in your pantry.");
        }
        else {
            pantryDataAccessObject.save(pantry);
            pantryPresenter.prepareSuccessView(new PantryOutputData(pantry));
        }
    }
}
