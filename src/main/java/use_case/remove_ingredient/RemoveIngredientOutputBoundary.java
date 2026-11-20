package use_case.remove_ingredient;

import use_case.pantry.PantryOutputData;

/**
 * The output boundary for the Remove Ingredient Use Case.
 */
public interface RemoveIngredientOutputBoundary {

    /**
     * Prepares the success view for the Remove Ingredient Use Case.
     * @param outputData the updated pantry snapshot
     */
    void prepareSuccessView(PantryOutputData outputData);

    /**
     * Prepares the failure view for the Remove Ingredient Use Case.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
