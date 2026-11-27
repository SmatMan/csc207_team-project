package use_case.find_recipes;

/**
 * The output boundary for the Find Recipes Use Case.
 */
public interface FindRecipesOutputBoundary {

    /**
     * Prepares the view listing the recipes found (none if the pantry is empty).
     * @param outputData the recipes and how many ingredients were searched
     */
    void prepareSuccessView(FindRecipesOutputData outputData);

    /**
     * Prepares the view for a search that couldn't be completed.
     * @param errorMessage the explanation of the failure
     */
    void prepareFailView(String errorMessage);
}
