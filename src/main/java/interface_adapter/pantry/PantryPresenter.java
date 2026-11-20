package interface_adapter.pantry;

import java.util.ArrayList;

import use_case.add_ingredient.AddIngredientOutputBoundary;
import use_case.load_pantry.LoadPantryOutputBoundary;
import use_case.pantry.PantryOutputData;
import use_case.remove_ingredient.RemoveIngredientOutputBoundary;

/**
 * The Presenter for the pantry use cases (load pantry, add ingredient and
 * remove ingredient). They all report the same pantry snapshot, which is shown
 * in the Ingredients tab.
 */
public class PantryPresenter implements LoadPantryOutputBoundary,
                                        AddIngredientOutputBoundary,
                                        RemoveIngredientOutputBoundary {

    private final PantryViewModel pantryViewModel;

    public PantryPresenter(PantryViewModel pantryViewModel) {
        this.pantryViewModel = pantryViewModel;
    }

    @Override
    public void prepareSuccessView(PantryOutputData response) {
        final PantryState state = pantryViewModel.getState();
        state.setIngredients(new ArrayList<>(response.getIngredientNames()));
        state.setError(null);
        pantryViewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        final PantryState state = pantryViewModel.getState();
        state.setError(error);
        pantryViewModel.notifyStateChanged();
    }
}
