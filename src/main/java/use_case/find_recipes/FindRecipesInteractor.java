package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import entity.Ingredient;
import entity.Recipe;

/**
 * The Find Recipes Interactor. Fetches recipes a page at a time; within a page, recipes
 * needing the fewest extra ingredients come first, so the user can cook without a grocery run.
 */
public class FindRecipesInteractor implements FindRecipesInputBoundary {

    /** How many recipes to ask for at a time. */
    public static final int PAGE_SIZE = 10;

    private final FindRecipesDataAccessInterface recipeDataAccessObject;
    private final FindRecipesOutputBoundary recipesPresenter;

    public FindRecipesInteractor(FindRecipesDataAccessInterface recipeDataAccessInterface,
                                 FindRecipesOutputBoundary findRecipesOutputBoundary) {
        this.recipeDataAccessObject = recipeDataAccessInterface;
        this.recipesPresenter = findRecipesOutputBoundary;
    }

    @Override
    public void execute(FindRecipesInputData findRecipesInputData) {
        final List<Ingredient> ingredients = new ArrayList<>();
        for (String name : findRecipesInputData.getIngredientNames()) {
            if (name != null && !name.isBlank()) {
                ingredients.add(new Ingredient(name));
            }
        }
        if (ingredients.isEmpty()) {
            // Nothing to search with: not an error, and no need to call the recipe service.
            recipesPresenter.prepareSuccessView(new FindRecipesOutputData(0, new ArrayList<>()));
            return;
        }

        final int offset = Math.max(0, findRecipesInputData.getOffset());
        final RecipeSearchResult result;
        try {
            result = recipeDataAccessObject.findRecipes(ingredients, findRecipesInputData.getFilter(), offset, PAGE_SIZE);
        }
        catch (RecipeSearchException ex) {
            recipesPresenter.prepareFailView(ex.getMessage());
            return;
        }

        final List<Recipe> recipes = new ArrayList<>(result.getRecipes());
        recipes.sort(Comparator.comparingInt((Recipe recipe) -> recipe.getMissingIngredients().size())
                .thenComparing(Comparator.comparingInt((Recipe recipe) -> recipe.getUsedIngredients().size())
                        .reversed()));

        final List<FoundRecipe> found = new ArrayList<>();
        for (Recipe recipe : recipes) {
            found.add(new FoundRecipe(recipe.getId(), recipe.getName(), recipe.getImage(),
                    names(recipe.getUsedIngredients()), names(recipe.getMissingIngredients()),
                    result.getDetails(recipe.getId())));
        }
        final boolean moreAvailable = !recipes.isEmpty() && offset + recipes.size() < result.getTotalResults();
        recipesPresenter.prepareSuccessView(new FindRecipesOutputData(ingredients.size(), found, offset, moreAvailable));
    }

    private static List<String> names(List<Ingredient> ingredients) {
        final List<String> names = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            names.add(ingredient.getName());
        }
        return names;
    }
}
