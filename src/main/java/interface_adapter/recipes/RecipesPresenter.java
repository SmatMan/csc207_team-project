package interface_adapter.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import use_case.find_recipes.FindRecipesOutputBoundary;
import use_case.find_recipes.FindRecipesOutputData;
import use_case.find_recipes.FoundRecipe;

/**
 * The Presenter for the Find Recipes Use Case. The first page replaces the recipes shown;
 * later pages are added after them. Details are formatted for display here.
 */
public class RecipesPresenter implements FindRecipesOutputBoundary {

    private final RecipesViewModel recipesViewModel;

    public RecipesPresenter(RecipesViewModel recipesViewModel) {
        this.recipesViewModel = recipesViewModel;
    }

    @Override
    public void prepareSuccessView(FindRecipesOutputData response) {
        final RecipesState state = recipesViewModel.getState();

        final List<RecipesState.RecipeCard> cards = new ArrayList<>();
        final Set<Integer> shown = new HashSet<>();
        if (response.getOffset() > 0) {
            cards.addAll(state.getRecipes());
            for (RecipesState.RecipeCard card : cards) {
                shown.add(card.getId());
            }
        }
        for (FoundRecipe recipe : response.getRecipes()) {
            // A recipe can turn up on two pages if results shift between requests; show it once.
            if (shown.add(recipe.getId())) {
                cards.add(card(recipe));
            }
        }

        state.setIngredientCount(response.getIngredientCount());
        state.setRecipes(cards);
        state.setMoreAvailable(response.isMoreAvailable());
        state.setError(null);
        recipesViewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        final RecipesState state = recipesViewModel.getState();
        state.setError(error);
        recipesViewModel.notifyStateChanged();
    }

    private static RecipesState.RecipeCard card(FoundRecipe recipe) {
        return new RecipesState.RecipeCard(recipe.getId(), recipe.getTitle(), recipe.getImageUrl(),
                recipe.getUsedIngredients().size(), recipe.getMissingIngredients(),
                outOf100(recipe.getScore()), outOf100(recipe.getHealthScore()),
                time(recipe.getReadyInMinutes()), servings(recipe.getServings()),
                recipe.getSummaryHtml(), recipe.getSteps(), time(recipe.getPreparationMinutes()));
    }

    /**
     * One of Spoonacular's 0-100 scores (its overall score or health score) as a whole
     * number, e.g. 73.5 to "74". These are computed by Spoonacular, not user ratings.
     */
    static String outOf100(Double score) {
        if (score == null) {
            return null;
        }
        return String.valueOf(Math.round(Math.max(0, Math.min(100, score))));
    }

    /**
     * A cooking time, e.g. 45 to "45 min" and 75 to "1 h 15 min".
     */
    static String time(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return null;
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        final int hours = minutes / 60;
        final int rest = minutes % 60;
        return rest == 0 ? hours + " h" : hours + " h " + rest + " min";
    }

    static String servings(Integer servings) {
        if (servings == null || servings <= 0) {
            return null;
        }
        return "Serves " + servings;
    }
}
