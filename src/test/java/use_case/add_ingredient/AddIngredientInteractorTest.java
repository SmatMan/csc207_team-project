package use_case.add_ingredient;

import java.util.List;

import data_access.InMemoryPantryDataAccessObject;
import entity.Ingredient;
import org.junit.jupiter.api.Test;
import use_case.pantry.PantryOutputData;

import static org.junit.jupiter.api.Assertions.*;

class AddIngredientInteractorTest {

    @Test
    void successTest() {
        final AddIngredientInputData inputData = new AddIngredientInputData("  Eggs ");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();
        pantryRepository.getPantry().add(new Ingredient("Rice"));

        final AddIngredientOutputBoundary successPresenter = new AddIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                assertEquals(List.of("Rice", "Eggs"), outputData.getIngredientNames());
                // The change was saved, not just reported.
                assertTrue(pantryRepository.getPantry().contains(new Ingredient("Eggs")));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final AddIngredientInputBoundary interactor =
                new AddIngredientInteractor(pantryRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureDuplicateIngredientTest() {
        final AddIngredientInputData inputData = new AddIngredientInputData("eggs");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();
        pantryRepository.getPantry().add(new Ingredient("Eggs"));

        final AddIngredientOutputBoundary failurePresenter = new AddIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("\"eggs\" is already in your pantry.", error);
                assertEquals(1, pantryRepository.getPantry().getIngredients().size());
            }
        };

        final AddIngredientInputBoundary interactor =
                new AddIngredientInteractor(pantryRepository, failurePresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureBlankIngredientTest() {
        final AddIngredientInputData inputData = new AddIngredientInputData("   ");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();

        final AddIngredientOutputBoundary failurePresenter = new AddIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Ingredient name cannot be empty.", error);
                assertTrue(pantryRepository.getPantry().getIngredients().isEmpty());
            }
        };

        final AddIngredientInputBoundary interactor =
                new AddIngredientInteractor(pantryRepository, failurePresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureEmptyIngredientTest() {
        final AddIngredientInputData inputData = new AddIngredientInputData("");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();

        final AddIngredientOutputBoundary failurePresenter = new AddIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Ingredient name cannot be empty.", error);
            }
        };

        final AddIngredientInputBoundary interactor =
                new AddIngredientInteractor(pantryRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
