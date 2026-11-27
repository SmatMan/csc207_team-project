package use_case.view_recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;

/**
 * Output Data for the View Recipe Use Case: everything needed to show one recipe.
 */
public class ViewRecipeOutputData {

    private final int id;
    private final String title;
    private final String imageUrl;
    private final RecipeDetails details;
    private final List<IngredientLine> have;
    private final List<IngredientLine> need;

    /**
     * Creates the output data.
     * @param id the recipe's id
     * @param title the recipe's title
     * @param imageUrl the address of its photo, or ""
     * @param details its description, steps, times and so on (fields may be unknown)
     * @param have the ingredients the user has, with quantities where known
     * @param need the ingredients the user would need to buy, with quantities where known
     */
    public ViewRecipeOutputData(int id, String title, String imageUrl, RecipeDetails details,
                                List<IngredientLine> have, List<IngredientLine> need) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.details = details;
        this.have = Collections.unmodifiableList(new ArrayList<>(have));
        this.need = Collections.unmodifiableList(new ArrayList<>(need));
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public RecipeDetails getDetails() {
        return details;
    }

    public List<IngredientLine> getHave() {
        return have;
    }

    public List<IngredientLine> getNeed() {
        return need;
    }
}
