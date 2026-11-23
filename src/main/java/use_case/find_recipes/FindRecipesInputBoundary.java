package use_case.find_recipes;

/**
 * Input Boundary for finding recipes that use the pantry's ingredients.
 */
public interface FindRecipesInputBoundary {

    /**
     * Executes the find recipes use case.
     * @param findRecipesInputData the input data
     */
    void execute(FindRecipesInputData findRecipesInputData);
}
