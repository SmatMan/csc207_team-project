package entity;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    private final Ingredient beef = new Ingredient("Beef");
    private final Ingredient chicken = new Ingredient("Chicken");
    private final Ingredient shrimp = new Ingredient("Shrimp");
    private final Ingredient salmon = new Ingredient("Salmon");
    private final Ingredient milk = new Ingredient("Milk");
    private final Ingredient cheese = new Ingredient("Cheese");

    private final DietPreset vegan = new DietPreset("Vegan",
            Set.of(beef, chicken, shrimp, salmon, milk, cheese));
    private final DietPreset pescatarian = new DietPreset("Pescatarian", Set.of(beef, chicken));
    private final DietPreset lactoseIntolerant = new DietPreset("Lactose intolerant", Set.of(milk, cheese));

    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile(List.of(
                new Intolerance("Meat", List.of(beef, chicken)),
                new Intolerance("Seafood", List.of(shrimp, salmon)),
                new Intolerance("Dairy", List.of(milk, cheese))));
    }

    @Test
    void newProfileExcludesNothing() {
        assertTrue(profile.getExcludedIngredients().isEmpty());
        assertTrue(profile.getActivePresets().isEmpty());
    }

    @Test
    void excludeAndAllowByHand() {
        profile.exclude(beef);
        assertTrue(profile.isExcluded(beef));
        assertFalse(profile.isExcluded(chicken));

        profile.allow(beef);
        assertFalse(profile.isExcluded(beef));
    }

    @Test
    void ingredientsCompareIgnoringCase() {
        profile.exclude(new Ingredient("shrimp"));
        assertTrue(profile.isExcluded(shrimp));
    }

    @Test
    void applyingPresetExcludesItsIngredients() {
        profile.applyPreset(pescatarian);
        assertTrue(profile.isPresetActive(pescatarian));
        assertEquals(List.of(beef, chicken), profile.getExcludedIngredients());
    }

    @Test
    void removingPresetKeepsManualExclusions() {
        // The shrimp scenario: manual untick survives a preset being turned on and off.
        profile.exclude(shrimp);
        profile.applyPreset(vegan);
        profile.removePreset(vegan);

        assertTrue(profile.isExcluded(shrimp));
        assertFalse(profile.isExcluded(beef));
        assertEquals(List.of(shrimp), profile.getExcludedIngredients());
    }

    @Test
    void stackedPresetsKeepSharedExclusions() {
        profile.applyPreset(vegan);
        profile.applyPreset(lactoseIntolerant);
        profile.removePreset(vegan);

        assertTrue(profile.isExcluded(milk));
        assertTrue(profile.isExcluded(cheese));
        assertFalse(profile.isExcluded(beef));
        assertFalse(profile.isExcluded(shrimp));
    }

    @Test
    void allowingOverridesActivePresetAndMarksItModified() {
        profile.applyPreset(pescatarian);
        assertFalse(profile.isPresetModified(pescatarian));

        profile.allow(chicken);

        assertFalse(profile.isExcluded(chicken));
        assertTrue(profile.isExcluded(beef));
        assertTrue(profile.isPresetModified(pescatarian));
    }

    @Test
    void reapplyingPresetClearsOverrides() {
        profile.applyPreset(pescatarian);
        profile.allow(chicken);
        profile.removePreset(pescatarian);
        profile.applyPreset(pescatarian);

        assertTrue(profile.isExcluded(chicken));
        assertFalse(profile.isPresetModified(pescatarian));
    }

    @Test
    void overrideIsForgottenWhenNoPresetExcludesIt() {
        profile.applyPreset(pescatarian);
        profile.allow(chicken);
        profile.removePreset(pescatarian);
        profile.applyPreset(vegan);

        // Vegan was turned on after the override, so chicken is excluded again.
        assertTrue(profile.isExcluded(chicken));
    }

    @Test
    void unknownIngredientIsRejected() {
        final Ingredient tofu = new Ingredient("Tofu");
        assertThrows(IllegalArgumentException.class, () -> profile.exclude(tofu));
        assertThrows(IllegalArgumentException.class, () -> profile.allow(tofu));
    }

    @Test
    void entitiesRejectBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> new Ingredient(" "));
        assertThrows(IllegalArgumentException.class, () -> new Intolerance("", List.of(beef)));
        assertThrows(IllegalArgumentException.class, () -> new Intolerance("Meat", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new DietPreset("Vegan", Set.of()));
    }
}
