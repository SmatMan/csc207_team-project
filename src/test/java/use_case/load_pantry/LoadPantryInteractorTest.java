package use_case.load_pantry;

import java.util.List;

import data_access.InMemoryPantryDataAccessObject;
import entity.Ingredient;
import org.junit.jupiter.api.Test;
import use_case.pantry.PantryOutputData;

import static org.junit.jupiter.api.Assertions.*;

class LoadPantryInteractorTest {

    @Test
    void successTest() {
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();
        pantryRepository.getPantry().add(new Ingredient("Eggs"));
        pantryRepository.getPantry().add(new Ingredient("Rice"));

        final LoadPantryOutputBoundary successPresenter = new LoadPantryOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                assertEquals(List.of("Eggs", "Rice"), outputData.getIngredientNames());
            }
        };

        final LoadPantryInputBoundary interactor = new LoadPantryInteractor(pantryRepository, successPresenter);
        interactor.execute();
    }

    @Test
    void emptyPantryTest() {
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();

        final LoadPantryOutputBoundary successPresenter = new LoadPantryOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                assertTrue(outputData.getIngredientNames().isEmpty());
            }
        };

        final LoadPantryInputBoundary interactor = new LoadPantryInteractor(pantryRepository, successPresenter);
        interactor.execute();
    }
}
