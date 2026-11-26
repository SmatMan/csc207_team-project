package use_case.view_recipe;

/**
 * The output boundary for the View Recipe Use Case.
 */
public interface ViewRecipeOutputBoundary {

    /**
     * Prepares the view showing the recipe in full.
     * @param outputData the recipe
     */
    void prepareSuccessView(ViewRecipeOutputData outputData);

    /**
     * Prepares the view for a recipe that couldn't be opened.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
