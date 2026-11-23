package use_case.create_profile;

import java.util.List;

import entity.Intolerance;

/**
 * The list of categories and ingredients a profile can be built from. The interactor
 * checks submissions against it without knowing where the list comes from
 * (a JSON file today, maybe the Spoonacular ingredient list later).
 */
public interface IntoleranceCatalogue {

    /**
     * Returns every category the user can pick from.
     * @return the available intolerance categories
     */
    List<Intolerance> getAvailableIntolerances();
}
