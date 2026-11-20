package use_case.remove_ingredient;

import entity.Pantry;

/**
 * DAO interface for the Remove Ingredient Use Case.
 */
public interface RemoveIngredientDataAccessInterface {

    /**
     * Returns the user's pantry.
     * @return the user's pantry
     */
    Pantry getPantry();

    /**
     * Saves the user's pantry.
     * @param pantry the pantry to save
     */
    void save(Pantry pantry);
}
