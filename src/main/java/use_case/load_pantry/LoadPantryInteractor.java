package use_case.load_pantry;

import use_case.pantry.PantryOutputData;

/**
 * The Load Pantry Interactor.
 */
public class LoadPantryInteractor implements LoadPantryInputBoundary {
    private final LoadPantryDataAccessInterface pantryDataAccessObject;
    private final LoadPantryOutputBoundary pantryPresenter;

    public LoadPantryInteractor(LoadPantryDataAccessInterface pantryDataAccessInterface,
                                LoadPantryOutputBoundary loadPantryOutputBoundary) {
        this.pantryDataAccessObject = pantryDataAccessInterface;
        this.pantryPresenter = loadPantryOutputBoundary;
    }

    @Override
    public void execute() {
        pantryPresenter.prepareSuccessView(new PantryOutputData(pantryDataAccessObject.getPantry()));
    }
}
