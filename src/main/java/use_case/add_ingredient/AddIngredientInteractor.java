package use_case.add_ingredient;

import entity.Ingredient;
import entity.Pantry;
import use_case.pantry.PantryOutputData;

/**
 * The Add Ingredient Interactor.
 */
public class AddIngredientInteractor implements AddIngredientInputBoundary {
    private final AddIngredientDataAccessInterface pantryDataAccessObject;
    private final AddIngredientOutputBoundary pantryPresenter;

    public AddIngredientInteractor(AddIngredientDataAccessInterface pantryDataAccessInterface,
                                   AddIngredientOutputBoundary addIngredientOutputBoundary) {
        this.pantryDataAccessObject = pantryDataAccessInterface;
        this.pantryPresenter = addIngredientOutputBoundary;
    }

    @Override
    public void execute(AddIngredientInputData addIngredientInputData) {
        final String name = addIngredientInputData.getIngredientName();
        if (name == null || name.isBlank()) {
            pantryPresenter.prepareFailView("Ingredient name cannot be empty.");
            return;
        }

        final Pantry pantry = pantryDataAccessObject.getPantry();
        final Ingredient ingredient = new Ingredient(name);
        if (!pantry.add(ingredient)) {
            pantryPresenter.prepareFailView("\"" + ingredient.getName() + "\" is already in your pantry.");
        }
        else {
            pantryDataAccessObject.save(pantry);
            pantryPresenter.prepareSuccessView(new PantryOutputData(pantry));
        }
    }
}
