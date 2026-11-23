package view.components;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientIconsTest {

    private final IngredientIcons icons = new IngredientIcons("/icons/ingredient-icons.json",
            Map.of("salmon", "Salmon", "beef", "Beef", "chicken", "Chicken", "tomatoes", "Tomatoes",
                    "tomato", "Tomatoes", "garlic", "Garlic", "cheese", "Cheese"),
            Map.of("Salmon", "Seafood", "Beef", "Meat", "Chicken", "Meat", "Tomatoes", "Vegetables",
                    "Garlic", "Vegetables", "Cheese", "Dairy & eggs"));

    @Test
    void categoriesFollowTheHierarchyWithUnknownLast() {
        final Map<String, List<String>> groups = icons.groupByCategory(
                List.of("Cheese", "dragonfruit", "Salmon", "Garlic", "Chicken"));
        assertEquals(List.of("Meat", "Seafood", "Vegetables", "Dairy & eggs", IngredientIcons.OTHER),
                List.copyOf(groups.keySet()));
    }

    @Test
    void ingredientsAreAlphabeticalWithinACategory() {
        final Map<String, List<String>> groups = icons.groupByCategory(
                List.of("tomato", "Garlic", "Chicken", "Beef"));
        assertEquals(List.of("Beef", "Chicken"), groups.get("Meat"));
        // Case-insensitive, and aliases ("tomato") land in their ingredient's category.
        assertEquals(List.of("Garlic", "tomato"), groups.get("Vegetables"));
    }

    @Test
    void emptyPantryHasNoSections() {
        assertTrue(icons.groupByCategory(List.of()).isEmpty());
    }
}
