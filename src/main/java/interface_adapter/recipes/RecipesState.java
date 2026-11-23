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
        private final String likesText;
        private final String timeText;
        private final String servingsText;
        private final List<String> diets;

        /**
         * Creates a recipe card without details.
         * @param title the recipe's title
         * @param imageUrl the address of its photo, or "" if it has none
         * @param usedCount how many of its ingredients are in the pantry
         * @param missingIngredients names of the ingredients still needed
         */
        public RecipeCard(String title, String imageUrl, int usedCount, List<String> missingIngredients) {
            this(0, title, imageUrl, usedCount, missingIngredients, null, null, null, null, new ArrayList<>());
        }

        /**
         * Creates a recipe card.
         * @param id the recipe's id
         * @param title the recipe's title
         * @param imageUrl the address of its photo, or "" if it has none
         * @param usedCount how many of its ingredients are in the pantry
         * @param missingIngredients names of the ingredients still needed
         * @param ratingText the rating out of 5, e.g. "3.7", or null
         * @param likesText the number of likes, e.g. "1.2k", or null
         * @param timeText how long it takes, e.g. "1 h 15 min", or null
         * @param servingsText how many it serves, e.g. "Serves 4", or null
         * @param diets diets it suits, e.g. "Gluten free"
         */
        public RecipeCard(int id, String title, String imageUrl, int usedCount, List<String> missingIngredients,
                          String ratingText, String likesText, String timeText, String servingsText,
                          List<String> diets) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
            this.usedCount = usedCount;
            this.missingIngredients = Collections.unmodifiableList(new ArrayList<>(missingIngredients));
            this.ratingText = ratingText;
            this.likesText = likesText;
            this.timeText = timeText;
            this.servingsText = servingsText;
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

        public String getLikesText() {
            return likesText;
        }

        public String getTimeText() {
            return timeText;
        }

        public String getServingsText() {
            return servingsText;
        }

        public List<String> getDiets() {
            return diets;
        }
    }
}
