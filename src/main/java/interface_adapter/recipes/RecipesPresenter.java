package interface_adapter.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
        final List<String> diets = new ArrayList<>();
        for (String diet : recipe.getDiets()) {
            diets.add(capitalise(diet));
        }
        return new RecipesState.RecipeCard(recipe.getId(), recipe.getTitle(), recipe.getImageUrl(),
                recipe.getUsedIngredients().size(), recipe.getMissingIngredients(),
                rating(recipe.getScore()), likes(recipe.getLikes()), time(recipe.getReadyInMinutes()),
                servings(recipe.getServings()), diets);
    }

    /**
     * A 0-100 score as a rating out of 5 with one decimal, e.g. 73.5 to "3.7".
     */
    static String rating(Double score) {
        if (score == null) {
            return null;
        }
        return String.format(Locale.ROOT, "%.1f", Math.max(0, Math.min(100, score)) / 20);
    }

    /**
     * A like count, shortened past a thousand, e.g. 1234 to "1.2k".
     */
    static String likes(Integer likes) {
        if (likes == null) {
            return null;
        }
        if (likes < 1000) {
            return String.valueOf(likes);
        }
        final double thousands = likes / 1000.0;
        if (thousands < 1000) {
            return trimZero(String.format(Locale.ROOT, "%.1f", thousands)) + "k";
        }
        return trimZero(String.format(Locale.ROOT, "%.1f", thousands / 1000)) + "M";
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

    private static String capitalise(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    private static String trimZero(String number) {
        return number.endsWith(".0") ? number.substring(0, number.length() - 2) : number;
    }
}
