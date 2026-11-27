package interface_adapter.recipe_detail;

import java.util.List;

import interface_adapter.ViewManagerModel;
import org.junit.jupiter.api.Test;
import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;
import use_case.view_recipe.ViewRecipeOutputData;

import static org.junit.jupiter.api.Assertions.*;

class RecipeDetailPresenterTest {

    @Test
    void quantitiesUseKitchenFractions() {
        assertEquals("1/2 cups", RecipeDetailPresenter.quantity(0.5, "cups"));
        assertEquals("1 1/2 tbsp", RecipeDetailPresenter.quantity(1.5, "tbsp"));
        assertEquals("2", RecipeDetailPresenter.quantity(2.0, ""));
        assertEquals("1/3", RecipeDetailPresenter.quantity(0.333, null));
        assertEquals("2.2 lb", RecipeDetailPresenter.quantity(2.2, "lb"));
        assertEquals("", RecipeDetailPresenter.quantity(null, "cups"));
    }

    @Test
    void successFillsTheRecipeScreenAndSwitchesToIt() {
        final ViewManagerModel viewManagerModel = new ViewManagerModel();
        final RecipeDetailViewModel viewModel = new RecipeDetailViewModel();
        final RecipeDetailPresenter presenter = new RecipeDetailPresenter(viewManagerModel, viewModel);

        presenter.prepareSuccessView(new ViewRecipeOutputData(7, "Egg Fried Rice", "",
                RecipeDetails.builder().readyInMinutes(75).preparationMinutes(10).servings(4)
                        .steps(List.of("Cook the rice.")).build(),
                List.of(new IngredientLine("eggs", 2.0, "", "2 eggs")),
                List.of(new IngredientLine("soy sauce", 2.0, "tbsp", "2 tbsp soy sauce"))));

        final RecipeDetailState state = viewModel.getState();
        assertEquals("Egg Fried Rice", state.getTitle());
        assertEquals("Ready in 1 h 15 min", state.getReadyText());
        assertEquals("Prep 10 min", state.getPrepText());
        assertEquals("Serves 4", state.getServingsText());
        assertEquals("Eggs", state.getHave().get(0).getName());
        assertEquals("2 tbsp", state.getNeed().get(0).getQuantity());
        assertEquals(RecipeDetailViewModel.VIEW_NAME, viewManagerModel.getCurrentViewName());
    }
}
