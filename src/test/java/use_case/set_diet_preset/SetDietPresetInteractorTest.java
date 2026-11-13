package use_case.set_diet_preset;

import java.util.Set;

import data_access.InMemoryUserProfileDataAccessObject;
import org.junit.jupiter.api.Test;
import use_case.ProfileTestData;
import use_case.profile.ProfileOutputData;

import static org.junit.jupiter.api.Assertions.*;

class SetDietPresetInteractorTest {

    @Test
    void turnOnSuccessTest() {
        final SetDietPresetInputData inputData = new SetDietPresetInputData("vegetarian", true);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();

        final SetDietPresetOutputBoundary successPresenter = new SetDietPresetOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                assertEquals(Set.of("Vegetarian"), outputData.getActivePresets());
                assertEquals(Set.of("Beef", "Chicken"), outputData.getExcludedIngredients());
                assertTrue(outputData.getModifiedPresets().isEmpty());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final SetDietPresetInputBoundary interactor = new SetDietPresetInteractor(profileRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void turnOffSuccessTest() {
        final SetDietPresetInputData inputData = new SetDietPresetInputData("Vegetarian", false);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();
        profileRepository.getProfile().applyPreset(profileRepository.getPreset("Vegetarian"));
        profileRepository.getProfile().applyPreset(profileRepository.getPreset("Lactose-free"));

        final SetDietPresetOutputBoundary successPresenter = new SetDietPresetOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                assertEquals(Set.of("Lactose-free"), outputData.getActivePresets());
                assertEquals(Set.of("Milk", "Cheese"), outputData.getExcludedIngredients());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        final SetDietPresetInputBoundary interactor = new SetDietPresetInteractor(profileRepository, successPresenter);
        interactor.execute(inputData);
    }

    @Test
    void failureUnknownPresetTest() {
        final SetDietPresetInputData inputData = new SetDietPresetInputData("Carnivore", true);
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();

        final SetDietPresetOutputBoundary failurePresenter = new SetDietPresetOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Unknown diet preset: \"Carnivore\".", error);
            }
        };

        final SetDietPresetInputBoundary interactor = new SetDietPresetInteractor(profileRepository, failurePresenter);
        interactor.execute(inputData);
    }
}
