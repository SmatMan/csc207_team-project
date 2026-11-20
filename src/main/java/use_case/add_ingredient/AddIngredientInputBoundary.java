package use_case.add_ingredient;

/**
 * Input Boundary for adding an ingredient to the pantry.
 */
public interface AddIngredientInputBoundary {

    /**
     * Executes the add ingredient use case.
     * @param addIngredientInputData the input data
     */
    void execute(AddIngredientInputData addIngredientInputData);
}
