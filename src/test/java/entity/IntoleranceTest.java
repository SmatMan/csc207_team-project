package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class IntoleranceTest {

    // The getters return exactly what we put in, in the same order.
    @Test
    void storesNameAndIngredients() {
        final Intolerance dairy = new Intolerance("Dairy", List.of(new Ingredient("Milk"), new Ingredient("Butter")));

        assertEquals("Dairy", dairy.getName());
        assertEquals(List.of(new Ingredient("Milk"), new Ingredient("Butter")), dairy.getIngredients());
    }

    // Spaces alone don't count as a name.
    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Intolerance("  ", List.of(new Ingredient("Milk"))));
    }

    // A category with nothing under it makes no sense, so the constructor refuses it.
    @Test
    void rejectsEmptyIngredientList() {
        assertThrows(IllegalArgumentException.class, () -> new Intolerance("Dairy", List.of()));
    }

    // "Milk" and "milk" are the same ingredient, so listing both is a duplicate.
    @Test
    void rejectsDuplicateIngredientsCaseInsensitively() {
        assertThrows(IllegalArgumentException.class,
                () -> new Intolerance("Dairy", List.of(new Ingredient("Milk"), new Ingredient("milk"))));
    }

    // Looking up "milk" finds "Milk".
    @Test
    void containsIsCaseInsensitive() {
        final Intolerance dairy = new Intolerance("Dairy", List.of(new Ingredient("Milk")));

        assertEquals(true, dairy.contains(new Ingredient("milk")));
    }
}
