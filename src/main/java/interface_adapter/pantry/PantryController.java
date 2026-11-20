package interface_adapter.pantry;

import use_case.add_ingredient.AddIngredientInputBoundary;
import use_case.add_ingredient.AddIngredientInputData;
import use_case.load_pantry.LoadPantryInputBoundary;
import use_case.remove_ingredient.RemoveIngredientInputBoundary;
import use_case.remove_ingredient.RemoveIngredientInputData;

/**
 * The controller for the Ingredients tab: loading the pantry,
 * and adding or removing single ingredients.
 */
public class PantryController {

    private final LoadPantryInputBoundary loadPantryUseCaseInteractor;
    private final AddIngredientInputBoundary addIngredientUseCaseInteractor;
    private final RemoveIngredientInputBoundary removeIngredientUseCaseInteractor;

    public PantryController(LoadPantryInputBoundary loadPantryUseCaseInteractor,
                            AddIngredientInputBoundary addIngredientUseCaseInteractor,
                            RemoveIngredientInputBoundary removeIngredientUseCaseInteractor) {
        this.loadPantryUseCaseInteractor = loadPantryUseCaseInteractor;
        this.addIngredientUseCaseInteractor = addIngredientUseCaseInteractor;
        this.removeIngredientUseCaseInteractor = removeIngredientUseCaseInteractor;
    }

    /**
     * Executes the Load Pantry Use Case.
     */
    public void loadPantry() {
        loadPantryUseCaseInteractor.execute();
    }

    /**
     * Executes the Add Ingredient Use Case.
     * @param ingredientName the ingredient to add
     */
    public void addIngredient(String ingredientName) {
        addIngredientUseCaseInteractor.execute(new AddIngredientInputData(ingredientName));
    }

    /**
     * Executes the Remove Ingredient Use Case.
     * @param ingredientName the ingredient to remove
     */
    public void removeIngredient(String ingredientName) {
        removeIngredientUseCaseInteractor.execute(new RemoveIngredientInputData(ingredientName));
    }
}
