package use_case.remove_ingredient;

/**
 * Input Boundary for removing an ingredient from the pantry.
 */
public interface RemoveIngredientInputBoundary {

    /**
     * Executes the remove ingredient use case.
     * @param removeIngredientInputData the input data
     */
    void execute(RemoveIngredientInputData removeIngredientInputData);
}
