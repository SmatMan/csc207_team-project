package use_case.add_ingredient;

import use_case.pantry.PantryOutputData;

/**
 * The output boundary for the Add Ingredient Use Case.
 */
public interface AddIngredientOutputBoundary {

    /**
     * Prepares the success view for the Add Ingredient Use Case.
     * @param outputData the updated pantry snapshot
     */
    void prepareSuccessView(PantryOutputData outputData);

    /**
     * Prepares the failure view for the Add Ingredient Use Case.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
