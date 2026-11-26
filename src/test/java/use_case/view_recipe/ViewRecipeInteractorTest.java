package use_case.view_recipe;

import java.util.List;

import data_access.InMemoryRecipeDataAccessObject;
import entity.Ingredient;
import entity.Recipe;
import org.junit.jupiter.api.Test;
import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;

import static org.junit.jupiter.api.Assertions.*;

class ViewRecipeInteractorTest {

    private static Recipe friedRice() {
        final Ingredient eggs = new Ingredient("eggs");
        final Ingredient rice = new Ingredient("rice");
        final Ingredient soySauce = new Ingredient("soy sauce");
        return new Recipe(7, "Egg Fried Rice", "https://img.example/7.jpg",
                List.of(eggs, rice, soySauce), List.of(soySauce), List.of(eggs, rice));
    }

    @Test
    void successWithDetailsTest() {
        final InMemoryRecipeDataAccessObject recipeRepository = new InMemoryRecipeDataAccessObject(List.of(friedRice()));
        recipeRepository.putDetails(7, RecipeDetails.builder()
                .summaryHtml("A <b>quick</b> dinner.")
                .steps(List.of("Cook the rice.", "Scramble the eggs.", "Stir everything together."))
                .readyInMinutes(20)
                .usedLines(List.of(new IngredientLine("eggs", 2.0, "", "2 eggs, beaten"),
                        new IngredientLine("rice", 3.0, "cups", "3 cups cooked rice")))
                .missedLines(List.of(new IngredientLine("soy sauce", 2.0, "tbsp", "2 tbsp soy sauce")))
                .build());

        // This creates a successPresenter that tests whether the test case is as we expect.
        final ViewRecipeOutputBoundary successPresenter = new ViewRecipeOutputBoundary() {
            @Override
            public void prepareSuccessView(ViewRecipeOutputData outputData) {
                assertEquals("Egg Fried Rice", outputData.getTitle());
                assertEquals(3, outputData.getDetails().getSteps().size());
                assertEquals("A <b>quick</b> dinner.", outputData.getDetails().getSummaryHtml());
                assertEquals(2, outputData.getHave().size());
                assertEquals("cups", outputData.getHave().get(1).getUnit());
                assertEquals("soy sauce", outputData.getNeed().get(0).getName());
                assertEquals(2.0, outputData.getNeed().get(0).getAmount());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        new ViewRecipeInteractor(recipeRepository, successPresenter).execute(new ViewRecipeInputData(7));
    }

    @Test
    void successWithoutDetailsUsesNamesTest() {
        final InMemoryRecipeDataAccessObject recipeRepository = new InMemoryRecipeDataAccessObject(List.of(friedRice()));

        final ViewRecipeOutputBoundary successPresenter = new ViewRecipeOutputBoundary() {
            @Override
            public void prepareSuccessView(ViewRecipeOutputData outputData) {
                // Like today's findByIngredients search: names, but no quantities or steps.
                assertEquals(List.of("eggs", "rice"),
                        List.of(outputData.getHave().get(0).getName(), outputData.getHave().get(1).getName()));
                assertNull(outputData.getHave().get(0).getAmount());
                assertEquals("soy sauce", outputData.getNeed().get(0).getName());
                assertTrue(outputData.getDetails().getSteps().isEmpty());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        new ViewRecipeInteractor(recipeRepository, successPresenter).execute(new ViewRecipeInputData(7));
    }

    @Test
    void failureUnknownRecipeTest() {
        final InMemoryRecipeDataAccessObject recipeRepository = new InMemoryRecipeDataAccessObject(List.of(friedRice()));

        final ViewRecipeOutputBoundary failurePresenter = new ViewRecipeOutputBoundary() {
            @Override
            public void prepareSuccessView(ViewRecipeOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("That recipe isn't available any more. Try searching again.", error);
            }
        };

        new ViewRecipeInteractor(recipeRepository, failurePresenter).execute(new ViewRecipeInputData(999));
    }
}
