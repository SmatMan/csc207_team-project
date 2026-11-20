package use_case.add_ingredient;

/**
 * The Input Data for the Add Ingredient Use Case.
 */
public class AddIngredientInputData {

    private final String ingredientName;

    public AddIngredientInputData(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    String getIngredientName() {
        return ingredientName;
    }

}
