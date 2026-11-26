package use_case.view_recipe;

/**
 * The Input Data for the View Recipe Use Case.
 */
public class ViewRecipeInputData {

    private final int recipeId;

    public ViewRecipeInputData(int recipeId) {
        this.recipeId = recipeId;
    }

    int getRecipeId() {
        return recipeId;
    }

}
