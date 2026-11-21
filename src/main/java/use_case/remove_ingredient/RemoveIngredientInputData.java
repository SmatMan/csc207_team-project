package use_case.remove_ingredient;

/**
 * The Input Data for the Remove Ingredient Use Case.
 */
public class RemoveIngredientInputData {

    private final String ingredientName;

    public RemoveIngredientInputData(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    String getIngredientName() {
        return ingredientName;
    }

}
