package use_case.view_recipe;

/**
 * Input Boundary for opening one recipe in full.
 */
public interface ViewRecipeInputBoundary {

    /**
     * Executes the view recipe use case.
     * @param viewRecipeInputData the input data
     */
    void execute(ViewRecipeInputData viewRecipeInputData);
}
