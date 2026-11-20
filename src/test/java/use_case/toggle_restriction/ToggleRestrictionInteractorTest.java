package use_case.toggle_restriction;

import java.util.Set;

import data_access.InMemoryUserProfileDataAccessObject;
import entity.Ingredient;
import org.junit.jupiter.api.Test;
import use_case.ProfileTestData;
import use_case.profile.ProfileOutputData;

import static org.junit.jupiter.api.Assertions.*;

class ToggleRestrictionInteractorTest {

    @Test
    void excludeSuccessTest() {
        final ToggleRestrictionInputData inputData = new ToggleRestrictionInputData("Beef", true);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();

        final ToggleRestrictionOutputBoundary successPresenter = new ToggleRestrictionOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                assertEquals(Set.of("Beef"), outputData.getExcludedIngredients());
                // The change was saved, not just reported.
                assertTrue(profileRepository.getProfile().isExcluded(new Ingredient("Beef")));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final ToggleRestrictionInputBoundary interactor =
                new ToggleRestrictionInteractor(profileRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void allowOverridesPresetAndMarksItModifiedTest() {
        final ToggleRestrictionInputData inputData = new ToggleRestrictionInputData("chicken", false);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();
        profileRepository.getProfile().applyPreset(profileRepository.getPreset("Vegetarian"));

        final ToggleRestrictionOutputBoundary successPresenter = new ToggleRestrictionOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                assertEquals(Set.of("Beef"), outputData.getExcludedIngredients());
                assertEquals(Set.of("Vegetarian"), outputData.getModifiedPresets());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final ToggleRestrictionInputBoundary interactor =
                new ToggleRestrictionInteractor(profileRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureUnknownIngredientTest() {
        final ToggleRestrictionInputData inputData = new ToggleRestrictionInputData("Tofu", true);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();

        final ToggleRestrictionOutputBoundary failurePresenter = new ToggleRestrictionOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("\"Tofu\" is not in any restriction category.", error);
            }
        };

        final ToggleRestrictionInputBoundary interactor =
                new ToggleRestrictionInteractor(profileRepository, failurePresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureBlankIngredientTest() {
        final ToggleRestrictionInputData inputData = new ToggleRestrictionInputData("  ", true);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();

        final ToggleRestrictionOutputBoundary failurePresenter = new ToggleRestrictionOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("No ingredient was chosen.", error);
            }
        };

        final ToggleRestrictionInputBoundary interactor =
                new ToggleRestrictionInteractor(profileRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
