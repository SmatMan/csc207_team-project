package data_access;

import java.util.HashSet;
import java.util.Set;

import entity.Ingredient;
import entity.Intolerance;
import entity.KnownIngredient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The two bundled data files must agree: every food on the Restrictions tab
 * must be something the user can add on the Ingredients tab.
 */
class CatalogueConsistencyTest {

    @Test
    void everyRestrictedFoodIsAKnownIngredient() {
        final Set<Ingredient> known = new HashSet<>();
        for (KnownIngredient ingredient
                : new FileIngredientCorpusDataAccessObject("/ingredients.json").getKnownIngredients()) {
            known.add(ingredient.getIngredient());
        }

        // A path that doesn't exist, so no saved profile is read or written.
        final FileUserProfileDataAccessObject restrictions =
                new FileUserProfileDataAccessObject("/restrictions.json", "target/no-such-profile.json");
        for (Intolerance intolerance : restrictions.getProfile().getIntolerances()) {
            for (Ingredient ingredient : intolerance.getIngredients()) {
                assertTrue(known.contains(ingredient),
                        "\"" + ingredient + "\" is in restrictions.json but not in ingredients.json");
            }
        }
    }
}
