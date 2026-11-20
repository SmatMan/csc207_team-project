package use_case.remove_ingredient;

import java.util.List;

import data_access.InMemoryPantryDataAccessObject;
import entity.Ingredient;
import org.junit.jupiter.api.Test;
import use_case.pantry.PantryOutputData;

import static org.junit.jupiter.api.Assertions.*;

class RemoveIngredientInteractorTest {

    @Test
    void successTest() {
        final RemoveIngredientInputData inputData = new RemoveIngredientInputData("EGGS");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();
        pantryRepository.getPantry().add(new Ingredient("Eggs"));
        pantryRepository.getPantry().add(new Ingredient("Rice"));

        final RemoveIngredientOutputBoundary successPresenter = new RemoveIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                assertEquals(List.of("Rice"), outputData.getIngredientNames());
                // The change was saved, not just reported.
                assertFalse(pantryRepository.getPantry().contains(new Ingredient("Eggs")));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final RemoveIngredientInputBoundary interactor =
                new RemoveIngredientInteractor(pantryRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureMissingIngredientTest() {
        final RemoveIngredientInputData inputData = new RemoveIngredientInputData("Tofu");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();
        pantryRepository.getPantry().add(new Ingredient("Eggs"));

        final RemoveIngredientOutputBoundary failurePresenter = new RemoveIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("\"Tofu\" is not in your pantry.", error);
                assertEquals(1, pantryRepository.getPantry().getIngredients().size());
            }
        };

        final RemoveIngredientInputBoundary interactor =
                new RemoveIngredientInteractor(pantryRepository, failurePresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureBlankIngredientTest() {
        final RemoveIngredientInputData inputData = new RemoveIngredientInputData("  ");
        final InMemoryPantryDataAccessObject pantryRepository = new InMemoryPantryDataAccessObject();

        final RemoveIngredientOutputBoundary failurePresenter = new RemoveIngredientOutputBoundary() {
            @Override
            public void prepareSuccessView(PantryOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("No ingredient was chosen.", error);
            }
        };

        final RemoveIngredientInputBoundary interactor =
                new RemoveIngredientInteractor(pantryRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
