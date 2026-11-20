package use_case.search_ingredients;

import java.util.List;

import data_access.InMemoryIngredientCorpusDataAccessObject;
import entity.Ingredient;
import entity.KnownIngredient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchIngredientsInteractorTest {

    private final SearchIngredientsDataAccessInterface ingredientRepository =
            new InMemoryIngredientCorpusDataAccessObject(List.of(
                    new KnownIngredient(new Ingredient("White rice"), "Grains", List.of("basmati rice")),
                    new KnownIngredient(new Ingredient("Brown rice"), "Grains", List.of()),
                    new KnownIngredient(new Ingredient("Milk"), "Dairy & eggs", List.of())));

    @Test
    void successTest() {
        final SearchIngredientsInputData inputData = new SearchIngredientsInputData("basmati rice");

        // This creates a successPresenter that tests whether the test case is as we expect.
        final SearchIngredientsOutputBoundary successPresenter = new SearchIngredientsOutputBoundary() {
            @Override
            public void prepareSuccessView(SearchIngredientsOutputData outputData) {
                assertEquals("basmati rice", outputData.getQuery());
                final IngredientSuggestion top = outputData.getSuggestions().get(0);
                assertEquals("White rice", top.getName());
                assertEquals("Grains", top.getCategory());
                assertEquals(MatchKind.ALIAS, top.getKind());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final SearchIngredientsInputBoundary interactor =
                new SearchIngredientsInteractor(ingredientRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void blankQueryGivesNoSuggestionsTest() {
        final SearchIngredientsInputData inputData = new SearchIngredientsInputData("  ");

        final SearchIngredientsOutputBoundary successPresenter = new SearchIngredientsOutputBoundary() {
            @Override
            public void prepareSuccessView(SearchIngredientsOutputData outputData) {
                assertTrue(outputData.getSuggestions().isEmpty());
            }

            @Override
            public void prepareFailView(String error) {
                fail("A blank query is not an error.");
            }
        };

        final SearchIngredientsInputBoundary interactor =
                new SearchIngredientsInteractor(ingredientRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureNoMatchTest() {
        final SearchIngredientsInputData inputData = new SearchIngredientsInputData(" xqzzv ");

        final SearchIngredientsOutputBoundary failurePresenter = new SearchIngredientsOutputBoundary() {
            @Override
            public void prepareSuccessView(SearchIngredientsOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("No ingredient matches \"xqzzv\".", error);
            }
        };

        final SearchIngredientsInputBoundary interactor =
                new SearchIngredientsInteractor(ingredientRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
