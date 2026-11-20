package use_case.load_pantry;

import use_case.pantry.PantryOutputData;

/**
 * The output boundary for the Load Pantry Use Case.
 */
public interface LoadPantryOutputBoundary {

    /**
     * Prepares the view showing the loaded pantry.
     * @param outputData the pantry snapshot
     */
    void prepareSuccessView(PantryOutputData outputData);
}
