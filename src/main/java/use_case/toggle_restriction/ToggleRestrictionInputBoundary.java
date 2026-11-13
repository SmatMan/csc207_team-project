package use_case.toggle_restriction;

/**
 * Input Boundary for excluding or allowing a single ingredient.
 */
public interface ToggleRestrictionInputBoundary {

    /**
     * Executes the toggle restriction use case.
     * @param toggleRestrictionInputData the input data
     */
    void execute(ToggleRestrictionInputData toggleRestrictionInputData);
}
