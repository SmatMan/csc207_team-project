package use_case.create_profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One category from the submission, with the ingredient names the user ticked under it.
 * Both are plain strings. The interactor matches them against the catalogue.
 */
public class IntoleranceInputData {

    private final String categoryName;
    private final List<String> ingredientNames;

    /**
     * Packages up the selection for one category.
     * @param categoryName the category name, e.g. "Dairy"
     * @param ingredientNames the ingredient names ticked under it
     */
    public IntoleranceInputData(String categoryName, List<String> ingredientNames) {
        this.categoryName = categoryName;
        // Copy and lock the list, same as the other input data class.
        this.ingredientNames = Collections.unmodifiableList(new ArrayList<>(ingredientNames));
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<String> getIngredientNames() {
        return ingredientNames;
    }
}
