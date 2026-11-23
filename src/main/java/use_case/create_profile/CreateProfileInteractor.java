package use_case.create_profile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;

/**
 * The logic for use case 3. It takes what the view submitted, checks every piece,
 * builds a UserProfile, and tries to save it. It never touches Swing, JSON or files.
 * It only knows the three interfaces it is handed (the catalogue, the data access
 * and the output boundary), which keeps it inside the use case layer.
 */
public class CreateProfileInteractor implements CreateProfileInputBoundary {

    private final IntoleranceCatalogue catalogue;
    private final CreateProfileDataAccessInterface dataAccess;
    private final CreateProfileOutputBoundary presenter;

    public CreateProfileInteractor(IntoleranceCatalogue catalogue, CreateProfileDataAccessInterface dataAccess,
                                    CreateProfileOutputBoundary presenter) {
        this.catalogue = catalogue;
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(CreateProfileInputData inputData) {
        // Step 1: the name. A blank one stops everything here. No other work, no save.
        final String name = inputData.getProfileName();
        if (name == null || name.isBlank()) {
            presenter.prepareFailView("Enter a profile name.");
            return;
        }
        final String trimmedName = name.trim();

        // Step 2: the selections. buildIntolerances throws on the first bad one,
        // and we pass its message straight to the presenter. The data access never runs.
        final List<Intolerance> intolerances;
        try {
            intolerances = buildIntolerances(inputData.getIntolerances());
        }
        catch (IllegalArgumentException ex) {
            presenter.prepareFailView(ex.getMessage());
            return;
        }

        // Step 3: build the profile. A new profile excludes nothing, so we exclude
        // every ingredient the user ticked ourselves.
        final UserProfile profile = new UserProfile(trimmedName, intolerances);
        for (Intolerance intolerance : intolerances) {
            for (Ingredient ingredient : intolerance.getIngredients()) {
                profile.exclude(ingredient);
            }
        }

        // Step 4: save. The lowercase name is the storage key, so "Home" and "home" collide.
        // The success message uses the display name the user typed, not the key.
        final String normalizedName = trimmedName.toLowerCase(Locale.ROOT);
        try {
            final boolean saved = dataAccess.saveIfNameAvailable(normalizedName, profile);
            if (saved) {
                presenter.prepareSuccessView(new CreateProfileOutputData(profile.getName()));
            }
            else {
                presenter.prepareFailView("That name is taken. Choose another name.");
            }
        }
        catch (ProfileStorageException ex) {
            presenter.prepareFailView("Saving failed. Try saving again.");
        }
    }

    // Checks each submitted category against the catalogue and turns it into an Intolerance.
    // Throws IllegalArgumentException on the first problem. execute() catches it.
    private List<Intolerance> buildIntolerances(List<IntoleranceInputData> submissions) {
        final List<Intolerance> available = catalogue.getAvailableIntolerances();
        final Set<String> seenCategories = new LinkedHashSet<>();
        final List<Intolerance> built = new ArrayList<>();

        for (IntoleranceInputData submission : submissions) {
            // The category has to exist in the catalogue (the match ignores case).
            final Intolerance category = findCategory(available, submission.getCategoryName());
            if (category == null) {
                throw new IllegalArgumentException("Unknown category: " + submission.getCategoryName());
            }
            // add() returns false when the category is already in the set,
            // which means it was submitted twice.
            if (!seenCategories.add(category.getName().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Category submitted more than once: " + category.getName());
            }
            if (submission.getIngredientNames().isEmpty()) {
                throw new IllegalArgumentException("Category \"" + category.getName() + "\" needs at least one ingredient");
            }

            final List<Ingredient> selected = new ArrayList<>();
            for (String ingredientName : submission.getIngredientNames()) {
                final Ingredient ingredient = new Ingredient(ingredientName);
                // The ingredient has to belong to this category. "Beef" under "Dairy" fails here.
                if (!category.contains(ingredient)) {
                    throw new IllegalArgumentException(
                            ingredient + " is not in category \"" + category.getName() + "\"");
                }
                // No repeats inside one category. Ingredient equality ignores case.
                if (selected.contains(ingredient)) {
                    throw new IllegalArgumentException("Duplicate ingredient: " + ingredient);
                }
                selected.add(ingredient);
            }

            // Build from the catalogue's category name, so the saved spelling is always the official one.
            // The same ingredient can sit under two different categories. That comes from the
            // catalogue, so it is not an error.
            built.add(new Intolerance(category.getName(), selected));
        }
        return built;
    }

    // Returns the catalogue category with this name (case-insensitive), or null if there is none.
    private static Intolerance findCategory(List<Intolerance> available, String name) {
        for (Intolerance intolerance : available) {
            if (intolerance.getName().equalsIgnoreCase(name)) {
                return intolerance;
            }
        }
        return null;
    }
}
