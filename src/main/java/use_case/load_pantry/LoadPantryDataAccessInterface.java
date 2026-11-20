package use_case.load_pantry;

import entity.Pantry;

/**
 * DAO interface for the Load Pantry Use Case.
 */
public interface LoadPantryDataAccessInterface {

    /**
     * Returns the user's saved pantry, or a new empty one if none was saved.
     * @return the user's pantry
     */
    Pantry getPantry();
}
