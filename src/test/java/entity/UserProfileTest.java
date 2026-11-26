package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class UserProfileTest {

    // One shared category for every test below.
    private final Intolerance dairy = new Intolerance("Dairy", List.of(new Ingredient("Milk"), new Ingredient("Butter")));

    // "  Home  " comes back as "Home".
    @Test
    void storesTrimmedNameAndIntolerances() {
        final UserProfile profile = new UserProfile("  Home  ", List.of(dairy));

        assertEquals("Home", profile.getName());
        assertEquals(List.of(dairy), profile.getIntolerances());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(" ", List.of(dairy)));
    }

    // A profile with no categories is valid. The user can save one with nothing ticked.
    @Test
    void allowsNoIntolerances() {
        final UserProfile profile = new UserProfile("Home", List.of());

        assertEquals(List.of(), profile.getIntolerances());
    }

    // exclude() marks the ingredient, and isExcluded() ignores case when it checks.
    @Test
    void excludeMarksIngredientExcluded() {
        final UserProfile profile = new UserProfile("Home", List.of(dairy));

        profile.exclude(new Ingredient("Milk"));

        assertTrue(profile.isExcluded(new Ingredient("milk")));
    }

    // Putting Milk in a category does not exclude it. Only exclude() does.
    @Test
    void buildingIntoleranceDoesNotAutoExclude() {
        final UserProfile profile = new UserProfile("Home", List.of(dairy));

        assertEquals(false, profile.isExcluded(new Ingredient("Milk")));
    }

    // Beef isn't in Dairy, so the profile refuses to exclude it.
    @Test
    void excludeRejectsIngredientNotInAnyIntolerance() {
        final UserProfile profile = new UserProfile("Home", List.of(dairy));

        assertThrows(IllegalArgumentException.class, () -> profile.exclude(new Ingredient("Beef")));
    }
}
