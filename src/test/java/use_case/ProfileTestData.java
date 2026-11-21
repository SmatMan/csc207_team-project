package use_case;

import java.util.List;
import java.util.Set;

import data_access.InMemoryUserProfileDataAccessObject;
import entity.DietPreset;
import entity.Ingredient;
import entity.Intolerance;

/**
 * A small catalogue shared by the dietary profile use case tests.
 */
public final class ProfileTestData {

    private ProfileTestData() {
    }

    /**
     * Creates an in-memory DAO with Meat (Beef, Chicken) and Dairy (Milk, Cheese) categories,
     * and Vegetarian and Lactose-free presets.
     * @return the DAO
     */
    public static InMemoryUserProfileDataAccessObject newDataAccessObject() {
        final Ingredient beef = new Ingredient("Beef");
        final Ingredient chicken = new Ingredient("Chicken");
        final Ingredient milk = new Ingredient("Milk");
        final Ingredient cheese = new Ingredient("Cheese");
        return new InMemoryUserProfileDataAccessObject(
                List.of(new Intolerance("Meat", List.of(beef, chicken)),
                        new Intolerance("Dairy", List.of(milk, cheese))),
                List.of(new DietPreset("Vegetarian", Set.of(beef, chicken)),
                        new DietPreset("Lactose-free", Set.of(milk, cheese))));
    }
}
