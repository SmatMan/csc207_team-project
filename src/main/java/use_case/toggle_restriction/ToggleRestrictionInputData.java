package use_case.toggle_restriction;

/**
 * The Input Data for the Toggle Restriction Use Case.
 */
public class ToggleRestrictionInputData {

    private final String ingredientName;
    private final boolean excluded;

    public ToggleRestrictionInputData(String ingredientName, boolean excluded) {
        this.ingredientName = ingredientName;
        this.excluded = excluded;
    }

    String getIngredientName() {
        return ingredientName;
    }

    boolean isExcluded() {
        return excluded;
    }

}
