package use_case.find_recipes;

import java.util.ArrayList;
import java.util.List;

import data_access.InMemoryRecipeDataAccessObject;
import entity.Ingredient;
import entity.Recipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FindRecipesInteractorTest {

    private static Recipe recipe(int id, String title, List<String> used, List<String> missing) {
        final List<Ingredient> usedIngredients = ingredients(used);
        final List<Ingredient> missingIngredients = ingredients(missing);
        final List<Ingredient> all = new ArrayList<>(usedIngredients);
        all.addAll(missingIngredients);
        return new Recipe(id, title, "https://img.example/" + id + ".jpg", all, missingIngredients, usedIngredients);
    }

    private static List<Ingredient> ingredients(List<String> names) {
        final List<Ingredient> ingredients = new ArrayList<>();
        for (String name : names) {
            ingredients.add(new Ingredient(name));
        }
        return ingredients;
    }

    @Test
    void successSortsByFewestMissingIngredientsTest() {
        final FindRecipesInputData inputData = new FindRecipesInputData(List.of("Eggs", "White rice", "Garlic"));
        final InMemoryRecipeDataAccessObject recipeRepository = new InMemoryRecipeDataAccessObject(List.of(
                recipe(1, "Garlic Bread", List.of("Garlic"), List.of("Bread", "Butter")),
                recipe(2, "Egg Fried Rice", List.of("Eggs", "White rice", "Garlic"), List.of("Soy sauce")),
                recipe(3, "Boiled Eggs", List.of("Eggs"), List.of("Salt"))));

        // This creates a successPresenter that tests whether the test case is as we expect.
        final FindRecipesOutputBoundary successPresenter = new FindRecipesOutputBoundary() {
            @Override
            public void prepareSuccessView(FindRecipesOutputData outputData) {
                assertEquals(3, outputData.getIngredientCount());
                final List<FoundRecipe> recipes = outputData.getRecipes();
                // One missing each: more ingredients used wins; then the recipe missing two.
                assertEquals("Egg Fried Rice", recipes.get(0).getTitle());
                assertEquals("Boiled Eggs", recipes.get(1).getTitle());
                assertEquals("Garlic Bread", recipes.get(2).getTitle());
                assertEquals(List.of("Soy sauce"), recipes.get(0).getMissingIngredients());
                assertEquals("https://img.example/2.jpg", recipes.get(0).getImageUrl());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final FindRecipesInputBoundary interactor = new FindRecipesInteractor(recipeRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void pagesThroughResultsTest() {
        final List<Recipe> twelve = new ArrayList<>();
        for (int id = 1; id <= 12; id++) {
            twelve.add(recipe(id, "Recipe " + id, List.of("Eggs"), List.of()));
        }
        final InMemoryRecipeDataAccessObject recipeRepository = new InMemoryRecipeDataAccessObject(twelve);
        final List<FindRecipesOutputData> pages = new ArrayList<>();

        final FindRecipesOutputBoundary successPresenter = new FindRecipesOutputBoundary() {
            @Override
            public void prepareSuccessView(FindRecipesOutputData outputData) {
                pages.add(outputData);
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final FindRecipesInputBoundary interactor = new FindRecipesInteractor(recipeRepository, successPresenter);
        interactor.execute(new FindRecipesInputData(List.of("Eggs")));
        interactor.execute(new FindRecipesInputData(List.of("Eggs"), FindRecipesInteractor.PAGE_SIZE));

        assertEquals(FindRecipesInteractor.PAGE_SIZE, pages.get(0).getRecipes().size());
        assertEquals(0, pages.get(0).getOffset());
        assertTrue(pages.get(0).isMoreAvailable());

        assertEquals(2, pages.get(1).getRecipes().size());
        assertEquals(FindRecipesInteractor.PAGE_SIZE, pages.get(1).getOffset());
        assertFalse(pages.get(1).isMoreAvailable());
    }

    @Test
    void emptyPantrySkipsTheSearchTest() {
        final FindRecipesInputData inputData = new FindRecipesInputData(List.of(" "));
        final InMemoryRecipeDataAccessObject recipeRepository =
                new InMemoryRecipeDataAccessObject(List.of(recipe(1, "Toast", List.of("Bread"), List.of())));

        final FindRecipesOutputBoundary successPresenter = new FindRecipesOutputBoundary() {
            @Override
            public void prepareSuccessView(FindRecipesOutputData outputData) {
                assertEquals(0, outputData.getIngredientCount());
                assertTrue(outputData.getRecipes().isEmpty());
            }

            @Override
            public void prepareFailView(String error) {
                fail("An empty pantry is not an error.");
            }
        };

        final FindRecipesInputBoundary interactor = new FindRecipesInteractor(recipeRepository, successPresenter);
        interactor.execute(inputData);
        // The recipe service is never called, so no quota is used.
        assertEquals(0, recipeRepository.getSearchCount());
    }

    @Test
    void failureServiceUnavailableTest() {
        final FindRecipesInputData inputData = new FindRecipesInputData(List.of("Eggs"));
        final InMemoryRecipeDataAccessObject recipeRepository = InMemoryRecipeDataAccessObject.failing();

        final FindRecipesOutputBoundary failurePresenter = new FindRecipesOutputBoundary() {
            @Override
            public void prepareSuccessView(FindRecipesOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Couldn't load recipes.", error);
            }
        };

        final FindRecipesInputBoundary interactor = new FindRecipesInteractor(recipeRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
