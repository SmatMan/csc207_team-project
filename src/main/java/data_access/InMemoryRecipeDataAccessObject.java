package data_access;

import java.util.ArrayList;
import java.util.List;

import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.FindRecipesDataAccessInterface;
import use_case.find_recipes.RecipeSearchException;
import use_case.find_recipes.RecipeSearchResult;

/**
 * In-memory implementation of the recipe DAO, for tests: pages through a fixed list of
 * recipes (or fails, if told to) and remembers how often it was asked.
 */
public class InMemoryRecipeDataAccessObject implements FindRecipesDataAccessInterface {

    private final List<Recipe> recipes;
    private final boolean failing;
    private int searchCount;

    /**
     * Creates a DAO that pages through the given recipes.
     * @param recipes every recipe that matches any search
     */
    public InMemoryRecipeDataAccessObject(List<Recipe> recipes) {
        this(recipes, false);
    }

    private InMemoryRecipeDataAccessObject(List<Recipe> recipes, boolean failing) {
        this.recipes = new ArrayList<>(recipes);
        this.failing = failing;
    }

    /**
     * Creates a DAO whose searches always fail, as if the recipe service were unreachable.
     * @return the failing DAO
     */
    public static InMemoryRecipeDataAccessObject failing() {
        return new InMemoryRecipeDataAccessObject(new ArrayList<>(), true);
    }

    @Override
    public RecipeSearchResult findRecipes(List<Ingredient> ingredients, int offset, int count)
            throws RecipeSearchException {
        searchCount++;
        if (failing) {
            throw new RecipeSearchException("Couldn't load recipes.", null);
        }
        final int from = Math.min(offset, recipes.size());
        final int to = Math.min(offset + count, recipes.size());
        return new RecipeSearchResult(recipes.subList(from, to), recipes.size());
    }

    public int getSearchCount() {
        return searchCount;
    }
}
