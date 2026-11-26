package data_access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import entity.Intolerance;

class FileIntoleranceCatalogueTest {

    // Loads the real restrictions.json and checks that it has categories, Dairy among them.
    @Test
    void readsCategoriesFromTheBundledCatalogue() {
        final FileIntoleranceCatalogue catalogue = new FileIntoleranceCatalogue("/restrictions.json");

        final List<Intolerance> available = catalogue.getAvailableIntolerances();

        assertFalse(available.isEmpty());
        assertTrue(available.stream().anyMatch(intolerance -> intolerance.getName().equals("Dairy")));
    }

    // A file that doesn't exist must throw. If the constructor returns, the test fails on purpose.
    @Test
    void throwsOnMissingResource() {
        try {
            new FileIntoleranceCatalogue("/does-not-exist.json");
            throw new AssertionError("expected a RuntimeException");
        }
        catch (RuntimeException expected) {
            // expected
        }
    }
}
