package use_case.load_profile;

import java.util.List;
import java.util.Set;

import data_access.InMemoryUserProfileDataAccessObject;
import entity.Ingredient;
import org.junit.jupiter.api.Test;
import use_case.ProfileTestData;
import use_case.profile.ProfileOutputData;

import static org.junit.jupiter.api.Assertions.*;

class LoadProfileInteractorTest {

    @Test
    void successTest() {
        final InMemoryUserProfileDataAccessObject profileRepository = ProfileTestData.newDataAccessObject();
        profileRepository.getProfile().exclude(new Ingredient("Beef"));

        // This creates a successPresenter that tests whether the test case is as we expect.
        final LoadProfileOutputBoundary successPresenter = new LoadProfileOutputBoundary() {
            @Override
            public void prepareSuccessView(ProfileOutputData outputData) {
                assertEquals(List.of("Meat", "Dairy"), List.copyOf(outputData.getIngredientsByCategory().keySet()));
                assertEquals(List.of("Beef", "Chicken"), outputData.getIngredientsByCategory().get("Meat"));
                assertEquals(Set.of("Beef"), outputData.getExcludedIngredients());
                assertEquals(List.of("Vegetarian", "Lactose-free"), outputData.getPresetNames());
                assertTrue(outputData.getActivePresets().isEmpty());
            }
        };

        final LoadProfileInputBoundary interactor = new LoadProfileInteractor(profileRepository, successPresenter);
        interactor.execute();
    }
}
