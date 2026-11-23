package data_access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.KnownIngredient;
import use_case.search_ingredients.SearchIngredientsDataAccessInterface;

/**
 * In-memory implementation of the ingredient corpus DAO, for tests.
 */
public class InMemoryIngredientCorpusDataAccessObject implements SearchIngredientsDataAccessInterface {

    private final List<KnownIngredient> knownIngredients;

    public InMemoryIngredientCorpusDataAccessObject(List<KnownIngredient> knownIngredients) {
        this.knownIngredients = Collections.unmodifiableList(new ArrayList<>(knownIngredients));
    }

    @Override
    public List<KnownIngredient> getKnownIngredients() {
        return knownIngredients;
    }
}
