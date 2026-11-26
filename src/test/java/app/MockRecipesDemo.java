package app;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import data_access.MockRecipeDataAccessObject;
import view.theme.Theme;

/**
 * Dev-only: runs the full app, but with saved mock recipe data instead of Spoonacular,
 * so the recipe cards (rating, likes, time, servings, diets) and infinite scroll can be
 * seen without API quota or a paged search. Recipes show once the pantry has something in it.
 * Run with: ./mvnw test-compile exec:java -Dexec.mainClass=app.MockRecipesDemo -Dexec.classpathScope=test
 */
public class MockRecipesDemo {

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            final JFrame application = new AppBuilder()
                    .useRecipeDataAccessObject(new MockRecipeDataAccessObject("/mock/recipes.json", 700))
                    .addPantryView()
                    .addAddIngredientView()
                    .addCreateProfileView()
                    .addRecipeDetailView()
                    .addSidebarView()
                    .addRecipesView()
                    .addFindRecipesUseCase()
                    .addViewRecipeUseCase()
                    .addPantryUseCases()
                    .addSearchIngredientsUseCase()
                    .addCreateProfileUseCase()
                    .build();
            application.setTitle("Leftasty (mock recipes)");
            application.setSize(1100, 720);
            application.setMinimumSize(new Dimension(800, 520));
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
    }
}
