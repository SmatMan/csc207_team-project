package interface_adapter.recipes;

import java.util.List;

import org.junit.jupiter.api.Test;
import use_case.find_recipes.FindRecipesOutputData;
import use_case.find_recipes.FoundRecipe;

import static org.junit.jupiter.api.Assertions.*;

class RecipesPresenterTest {

    private final RecipesViewModel viewModel = new RecipesViewModel();
    private final RecipesPresenter presenter = new RecipesPresenter(viewModel);

    private static FoundRecipe recipe(int id) {
        return new FoundRecipe(id, "Recipe " + id, "", List.of("Eggs"), List.of("Salt"));
    }

    @Test
    void laterPagesAreAddedWithoutRepeats() {
        presenter.prepareSuccessView(new FindRecipesOutputData(1, List.of(recipe(1), recipe(2)), 0, true));
        presenter.prepareSuccessView(new FindRecipesOutputData(1, List.of(recipe(2), recipe(3)), 2, false));

        final List<RecipesState.RecipeCard> cards = viewModel.getState().getRecipes();
        assertEquals(List.of(1, 2, 3), List.of(cards.get(0).getId(), cards.get(1).getId(), cards.get(2).getId()));
        assertFalse(viewModel.getState().isMoreAvailable());
    }

    @Test
    void firstPageReplacesEarlierResults() {
        presenter.prepareSuccessView(new FindRecipesOutputData(1, List.of(recipe(1), recipe(2)), 0, true));
        presenter.prepareSuccessView(new FindRecipesOutputData(2, List.of(recipe(9)), 0, false));

        assertEquals(1, viewModel.getState().getRecipes().size());
        assertEquals(9, viewModel.getState().getRecipes().get(0).getId());
    }

    @Test
    void detailsAreFormattedForTheCard() {
        presenter.prepareSuccessView(new FindRecipesOutputData(1, List.of(
                new FoundRecipe(5, "Kappa Maki", "", List.of("White rice"), List.of("Nori"),
                        73.48, 1234, 75, 8, List.of("gluten free", "dairy free"))), 0, false));

        final RecipesState.RecipeCard card = viewModel.getState().getRecipes().get(0);
        assertEquals("3.7", card.getRatingText());
        assertEquals("1.2k", card.getLikesText());
        assertEquals("1 h 15 min", card.getTimeText());
        assertEquals("Serves 8", card.getServingsText());
        assertEquals(List.of("Gluten free", "Dairy free"), card.getDiets());
    }

    @Test
    void missingDetailsStayEmpty() {
        presenter.prepareSuccessView(new FindRecipesOutputData(1, List.of(recipe(1)), 0, false));

        final RecipesState.RecipeCard card = viewModel.getState().getRecipes().get(0);
        assertNull(card.getRatingText());
        assertNull(card.getLikesText());
        assertNull(card.getTimeText());
        assertNull(card.getServingsText());
        assertTrue(card.getDiets().isEmpty());
    }

    @Test
    void numberFormatting() {
        assertEquals("999", RecipesPresenter.likes(999));
        assertEquals("1k", RecipesPresenter.likes(1000));
        assertEquals("2.5M", RecipesPresenter.likes(2_500_000));
        assertEquals("45 min", RecipesPresenter.time(45));
        assertEquals("2 h", RecipesPresenter.time(120));
        assertEquals("5.0", RecipesPresenter.rating(100.0));
    }
}
