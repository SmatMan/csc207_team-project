package use_case.search_ingredients;

import java.util.List;

import entity.KnownIngredient;

/**
 * DAO interface for the Search Ingredients Use Case.
 */
public interface SearchIngredientsDataAccessInterface {

    /**
     * Returns every ingredient the app knows about.
     * @return the known ingredients
     */
    List<KnownIngredient> getKnownIngredients();
}
