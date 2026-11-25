package use_case.create_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import entity.Ingredient;
import entity.Intolerance;
import entity.UserProfile;

class CreateProfileInteractorTest {

    // A small fixed catalogue. The lambda below works because IntoleranceCatalogue has one method.
    private static final List<Intolerance> CATALOGUE = List.of(
            new Intolerance("Dairy", List.of(new Ingredient("Milk"), new Ingredient("Butter"))),
            new Intolerance("Meat", List.of(new Ingredient("Beef"), new Ingredient("Chicken"))));

    private final IntoleranceCatalogue catalogue = () -> CATALOGUE;

    // Happy path. Milk gets ticked and Butter doesn't, so only Milk ends up excluded.
    @Test
    void createsProfileWithSelectedIngredientsExcluded() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Dairy", List.of("Milk")))));

        assertEquals("Home", presenter.successData.getProfileName());
        assertNull(presenter.failMessage);
        assertEquals("Home", dataAccess.savedProfile.getName());
        assertTrue(dataAccess.savedProfile.isExcluded(new Ingredient("Milk")));
        assertFalse(dataAccess.savedProfile.isExcluded(new Ingredient("Butter")));
    }

    // A name with nothing ticked is still a valid profile.
    @Test
    void allowsNoIntolerances() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home", List.of()));

        assertEquals("Home", presenter.successData.getProfileName());
        assertTrue(dataAccess.savedProfile.getIntolerances().isEmpty());
    }

    // Egg appears under both A and B in this catalogue. Selecting it under both is fine,
    // because the overlap comes from the catalogue and not from the user.
    @Test
    void sameIngredientMaySelectedUnderTwoDifferentCategoriesIfCatalogueAllowsIt() {
        final List<Intolerance> catalogueWithOverlap = List.of(
                new Intolerance("A", List.of(new Ingredient("Egg"))),
                new Intolerance("B", List.of(new Ingredient("Egg"))));
        final IntoleranceCatalogue overlapCatalogue = () -> catalogueWithOverlap;
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor =
                new CreateProfileInteractor(overlapCatalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("A", List.of("Egg")), new IntoleranceInputData("B", List.of("Egg")))));

        assertNull(presenter.failMessage);
        assertTrue(dataAccess.savedProfile.isExcluded(new Ingredient("Egg")));
    }

    // Every rejection test below checks two things: the presenter got no success,
    // and the data access was never asked to save.
    @Test
    void rejectsBlankName() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("   ", List.of()));

        assertNull(presenter.successData);
        assertEquals(true, presenter.failMessage != null);
        assertFalse(dataAccess.saveWasCalled);
    }

    // Shellfish isn't in the catalogue.
    @Test
    void rejectsUnknownCategory() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Shellfish", List.of("Shrimp")))));

        assertNull(presenter.successData);
        assertFalse(dataAccess.saveWasCalled);
    }

    // Dairy shows up twice in one submission.
    @Test
    void rejectsDuplicateCategoryInSubmission() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Dairy", List.of("Milk")),
                        new IntoleranceInputData("Dairy", List.of("Butter")))));

        assertNull(presenter.successData);
        assertFalse(dataAccess.saveWasCalled);
    }

    // A category submitted with no ingredients.
    @Test
    void rejectsEmptyIngredientListForACategory() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Dairy", List.of()))));

        assertNull(presenter.successData);
        assertFalse(dataAccess.saveWasCalled);
    }

    // "Milk" and "milk" in the same category count as one ingredient twice.
    @Test
    void rejectsDuplicateIngredientWithinOneCategoryCaseInsensitively() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Dairy", List.of("Milk", "milk")))));

        assertNull(presenter.successData);
        assertFalse(dataAccess.saveWasCalled);
    }

    // Beef belongs to Meat, so it can't go under Dairy.
    @Test
    void rejectsIngredientNotInTheSubmittedCategory() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home",
                List.of(new IntoleranceInputData("Dairy", List.of("Beef")))));

        assertNull(presenter.successData);
        assertFalse(dataAccess.saveWasCalled);
    }

    // The data access says the name is taken. The save was called here (that is how the
    // interactor finds out), and the message tells the user to pick another name.
    @Test
    void duplicateNameProducesFailMessageAskingForAnotherName() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        dataAccess.nameTaken = true;
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home", List.of()));

        assertNull(presenter.successData);
        assertTrue(dataAccess.saveWasCalled);
        assertEquals(true, presenter.failMessage.toLowerCase(Locale.ROOT).contains("name"));
    }

    // The data access throws a storage error. The message tells the user to try again.
    @Test
    void storageFailureProducesFailMessageAskingToTryAgain() {
        final RecordingPresenter presenter = new RecordingPresenter();
        final FakeDataAccess dataAccess = new FakeDataAccess();
        dataAccess.throwOnSave = true;
        final CreateProfileInputBoundary interactor = new CreateProfileInteractor(catalogue, dataAccess, presenter);

        interactor.execute(new CreateProfileInputData("Home", List.of()));

        assertNull(presenter.successData);
        assertEquals(true, presenter.failMessage.toLowerCase(Locale.ROOT).contains("try"));
    }

    // Fake presenter. It stores whatever the interactor sends so the tests can read it.
    private static final class RecordingPresenter implements CreateProfileOutputBoundary {
        private CreateProfileOutputData successData;
        private String failMessage;

        @Override
        public void prepareSuccessView(CreateProfileOutputData outputData) {
            this.successData = outputData;
        }

        @Override
        public void prepareFailView(String message) {
            this.failMessage = message;
        }
    }

    // Fake data access. nameTaken and throwOnSave switch on the two failure paths.
    // saveWasCalled and savedProfile let the tests see what the interactor did.
    private static final class FakeDataAccess implements CreateProfileDataAccessInterface {
        private boolean nameTaken;
        private boolean throwOnSave;
        private boolean saveWasCalled;
        private UserProfile savedProfile;

        @Override
        public boolean saveIfNameAvailable(String normalizedName, UserProfile profile) throws ProfileStorageException {
            saveWasCalled = true;
            if (throwOnSave) {
                throw new ProfileStorageException("storage failed", null);
            }
            if (nameTaken) {
                return false;
            }
            this.savedProfile = profile;
            return true;
        }
    }
}
