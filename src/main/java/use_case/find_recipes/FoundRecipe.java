package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One recipe found for the pantry, as plain data for the presenter. The details
 * (score, likes, time, servings, diets) are null or empty when the recipe service
 * doesn't provide them.
 */
public class FoundRecipe {

    private final int id;
    private final String title;
    private final String imageUrl;
    private final List<String> usedIngredients;
    private final List<String> missingIngredients;
    private final Double score;
    private final Integer likes;
    private final Integer readyInMinutes;
    private final Integer servings;
    private final List<String> diets;

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
        this(id, title, imageUrl, usedIngredients, missingIngredients, null, null, null, null, new ArrayList<>());
    }

    /**
     * Creates a found recipe with details.
     * @param id the recipe's id
     * @param title the recipe's title
     * @param imageUrl the address of the recipe's photo, or "" if it has none
     * @param usedIngredients names of the ingredients the user already has
     * @param missingIngredients names of the ingredients the user would need to buy
     * @param score the recipe's quality score from 0 to 100, or null
     * @param likes how many people liked the recipe, or null
     * @param readyInMinutes how long it takes to make, or null
     * @param servings how many people it serves, or null
     * @param diets diets the recipe suits, e.g. "gluten free" (empty if unknown)
     */
    public FoundRecipe(int id, String title, String imageUrl,
                       List<String> usedIngredients, List<String> missingIngredients,
                       Double score, Integer likes, Integer readyInMinutes, Integer servings,
                       List<String> diets) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.usedIngredients = Collections.unmodifiableList(new ArrayList<>(usedIngredients));
        this.missingIngredients = Collections.unmodifiableList(new ArrayList<>(missingIngredients));
        this.score = score;
        this.likes = likes;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.diets = Collections.unmodifiableList(new ArrayList<>(diets));
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
        return score;
    }

    public Integer getLikes() {
        return likes;
    }

    public Integer getReadyInMinutes() {
        return readyInMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public List<String> getDiets() {
        return diets;
    }
}
