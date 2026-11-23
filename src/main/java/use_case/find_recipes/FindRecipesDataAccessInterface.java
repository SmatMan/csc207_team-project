package use_case.find_recipes;

import java.util.List;

import entity.Ingredient;

/**
 * DAO interface for the Find Recipes Use Case.
 */
public interface FindRecipesDataAccessInterface {

    /**
     * Finds one page of recipes that use some of the given ingredients.
     * @param ingredients the ingredients the user has
     * @param offset how many matching recipes to skip (0 for the first page)
     * @param count the most recipes to return
     * @return the page of recipes, each listing the ingredients it uses and the ones that are
     *     missing, plus how many recipes match in total
     * @throws RecipeSearchException if the recipes can't be fetched
     */
    RecipeSearchResult findRecipes(List<Ingredient> ingredients, int offset, int count)
            throws RecipeSearchException;
}
