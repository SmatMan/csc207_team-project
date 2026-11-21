package use_case.add_ingredient;

import entity.Pantry;

/**
 * DAO interface for the Add Ingredient Use Case.
 */
public interface AddIngredientDataAccessInterface {

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
