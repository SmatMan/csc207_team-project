package interface_adapter.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The state for the Recipes View Model.
 */
public class RecipesState {
    private int ingredientCount;
    private List<RecipeCard> recipes = new ArrayList<>();
    private boolean moreAvailable;
    private String error;

    /**
     * Returns how many pantry ingredients the last search used (0 if the pantry was empty).
     * @return the ingredient count
     */
    public int getIngredientCount() {
        return ingredientCount;
    }

    /**
     * Returns every recipe loaded so far, page after page.
     * @return the recipes
     */
    public List<RecipeCard> getRecipes() {
        return recipes;
    }

    /**
     * Returns whether scrolling further can load more recipes.
     * @return true if another page is available
     */
    public boolean isMoreAvailable() {
        return moreAvailable;
    }

    public String getError() {
        return error;
    }

    public void setIngredientCount(int ingredientCount) {
        this.ingredientCount = ingredientCount;
    }

    public void setRecipes(List<RecipeCard> recipes) {
        this.recipes = recipes;
    }

    public void setMoreAvailable(boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * One recipe, as its card shows it. The detail texts are null when unknown.
     */
    public static class RecipeCard {
        private final int id;
        private final String title;
        private final String imageUrl;
        private final int usedCount;
        private final List<String> missingIngredients;
        private final String ratingText;
        private final String healthText;
        private final String timeText;
        private final String servingsText;
        private final String summaryHtml;
        private final List<String> steps;
        private final String prepText;

        /**
         * Creates a recipe card without details.
         * @param title the recipe's title
         * @param imageUrl the address of its photo, or "" if it has none
         * @param usedCount how many of its ingredients are in the pantry
         * @param missingIngredients names of the ingredients still needed
         */
        public RecipeCard(String title, String imageUrl, int usedCount, List<String> missingIngredients) {
            this(0, title, imageUrl, usedCount, missingIngredients, null, null, null, null);
        }

        /**
         * Creates a recipe card without a preview (description, steps and prep time).
         */
        public RecipeCard(int id, String title, String imageUrl, int usedCount, List<String> missingIngredients,
                          String ratingText, String healthText, String timeText, String servingsText) {
            this(id, title, imageUrl, usedCount, missingIngredients, ratingText, healthText, timeText, servingsText,
                    "", new ArrayList<>(), null);
        }

        /**
         * Creates a recipe card.
         * @param id the recipe's id
         * @param title the recipe's title
         * @param imageUrl the address of its photo, or "" if it has none
         * @param usedCount how many of its ingredients are in the pantry
         * @param missingIngredients names of the ingredients still needed
         * @param ratingText Spoonacular's score out of 100, e.g. "83", or null
         * @param healthText Spoonacular's health score out of 100, e.g. "30", or null
         * @param timeText how long it takes, e.g. "1 h 15 min", or null
         * @param servingsText how many it serves, e.g. "Serves 4", or null
         * @param summaryHtml the description, as HTML (bold facts, links), or ""
         * @param steps the method, one step per entry (empty if unknown)
         * @param prepText the preparation time, e.g. "10 min", or null
         */
        public RecipeCard(int id, String title, String imageUrl, int usedCount, List<String> missingIngredients,
                          String ratingText, String healthText, String timeText, String servingsText,
                          String summaryHtml, List<String> steps, String prepText) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
            this.usedCount = usedCount;
            this.missingIngredients = Collections.unmodifiableList(new ArrayList<>(missingIngredients));
            this.ratingText = ratingText;
            this.healthText = healthText;
            this.timeText = timeText;
            this.servingsText = servingsText;
            this.summaryHtml = summaryHtml == null ? "" : summaryHtml;
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
            this.prepText = prepText;
        }

        public String getSummaryHtml() {
            return summaryHtml;
        }

        public List<String> getSteps() {
            return steps;
        }

        public String getPrepText() {
            return prepText;
        }

        /**
         * Returns whether there's anything to show in a hover preview.
         * @return true if there's a description or steps
         */
        public boolean hasPreview() {
            return !summaryHtml.isBlank() || !steps.isEmpty();
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

        public int getUsedCount() {
            return usedCount;
        }

        public int getTotalCount() {
            return usedCount + missingIngredients.size();
        }

        public List<String> getMissingIngredients() {
            return missingIngredients;
        }

        public String getRatingText() {
            return ratingText;
        }

        public String getHealthText() {
            return healthText;
        }

        public String getTimeText() {
            return timeText;
        }

        public String getServingsText() {
            return servingsText;
        }
    }
}
