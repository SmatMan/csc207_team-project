package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One recipe found for the pantry, as plain data for the presenter. The details
 * (score, health score, likes, time, servings, diets) are null or empty when the
 * recipe service doesn't provide them.
 */
public class FoundRecipe {

    private static final RecipeDetails NO_DETAILS = RecipeDetails.builder().build();

    private final int id;
    private final String title;
    private final String imageUrl;
    private final List<String> usedIngredients;
    private final List<String> missingIngredients;
    private final RecipeDetails details;

    /**
     * Creates a found recipe without details.
     * @param id the recipe's id
     * @param title the recipe's title
     * @param imageUrl the address of the recipe's photo, or "" if it has none
     * @param usedIngredients names of the ingredients the user already has
     * @param missingIngredients names of the ingredients the user would need to buy
     */
    public FoundRecipe(int id, String title, String imageUrl,
                       List<String> usedIngredients, List<String> missingIngredients) {
        this(id, title, imageUrl, usedIngredients, missingIngredients, null);
    }

    /**
     * Creates a found recipe with details.
     * @param id the recipe's id
     * @param title the recipe's title
     * @param imageUrl the address of the recipe's photo, or "" if it has none
     * @param usedIngredients names of the ingredients the user already has
     * @param missingIngredients names of the ingredients the user would need to buy
     * @param details extra information from the recipe service, or null if there is none
     */
    public FoundRecipe(int id, String title, String imageUrl,
                       List<String> usedIngredients, List<String> missingIngredients, RecipeDetails details) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.usedIngredients = Collections.unmodifiableList(new ArrayList<>(usedIngredients));
        this.missingIngredients = Collections.unmodifiableList(new ArrayList<>(missingIngredients));
        this.details = details == null ? NO_DETAILS : details;
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

    public List<String> getUsedIngredients() {
        return usedIngredients;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public Double getScore() {
        return details.getScore();
    }

    public Double getHealthScore() {
        return details.getHealthScore();
    }

    public Integer getLikes() {
        return details.getLikes();
    }

    public Integer getReadyInMinutes() {
        return details.getReadyInMinutes();
    }

    public Integer getServings() {
        return details.getServings();
    }

    public List<String> getDiets() {
        return details.getDiets();
    }

    public Integer getPreparationMinutes() {
        return details.getPreparationMinutes();
    }

    /**
     * Returns the recipe's description: HTML, with bold facts and links.
     * @return the description, or "" if there is none
     */
    public String getSummaryHtml() {
        return details.getSummaryHtml();
    }

    public List<String> getSteps() {
        return details.getSteps();
    }
}
