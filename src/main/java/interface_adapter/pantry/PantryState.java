package interface_adapter.pantry;

import java.util.ArrayList;
import java.util.List;

/**
 * The state for the Pantry View Model.
 */
public class PantryState {
    private List<String> ingredients = new ArrayList<>();
    private String error;

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getError() {
        return error;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setError(String error) {
        this.error = error;
    }

}
