package entity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PantryTest {

    private final Ingredient eggs = new Ingredient("Eggs");
    private final Ingredient rice = new Ingredient("Rice");

    private Pantry pantry;

    @BeforeEach
    void setUp() {
        pantry = new Pantry();
    }

    @Test
    void newPantryIsEmpty() {
        assertTrue(pantry.getIngredients().isEmpty());
    }

    @Test
    void addKeepsInsertionOrder() {
        assertTrue(pantry.add(rice));
        assertTrue(pantry.add(eggs));
        assertEquals(List.of(rice, eggs), pantry.getIngredients());
    }

    @Test
    void addIgnoresCaseInsensitiveDuplicate() {
        pantry.add(eggs);
        assertFalse(pantry.add(new Ingredient("EGGS")));
        assertEquals(1, pantry.getIngredients().size());
        assertEquals("Eggs", pantry.getIngredients().get(0).getName());
    }

    @Test
    void removeIsCaseInsensitive() {
        pantry.add(eggs);
        assertTrue(pantry.remove(new Ingredient("eggs")));
        assertFalse(pantry.contains(eggs));
    }

    @Test
    void removeMissingReturnsFalse() {
        assertFalse(pantry.remove(eggs));
    }

    @Test
    void savedPantryDropsDuplicates() {
        final Pantry saved = new Pantry(List.of(eggs, rice, new Ingredient("rice")));
        assertEquals(List.of(eggs, rice), saved.getIngredients());
    }

    @Test
    void ingredientsCannotBeChangedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> pantry.getIngredients().add(eggs));
    }
}
