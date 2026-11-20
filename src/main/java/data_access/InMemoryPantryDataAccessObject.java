package data_access;

import entity.Pantry;
import use_case.add_ingredient.AddIngredientDataAccessInterface;
import use_case.load_pantry.LoadPantryDataAccessInterface;

/**
 * In-memory implementation of the DAO for the user's pantry. This implementation
 * does NOT persist data between runs of the program.
 */
public class InMemoryPantryDataAccessObject implements LoadPantryDataAccessInterface,
                                                       AddIngredientDataAccessInterface {

    private Pantry pantry = new Pantry();

    @Override
    public Pantry getPantry() {
        return pantry;
    }

    @Override
    public void save(Pantry userPantry) {
        this.pantry = userPantry;
    }
}
