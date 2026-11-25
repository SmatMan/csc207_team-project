package app;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import API.ApiCall;
import entity.Ingredient;
import view.theme.Theme;

/**
 * Entry point of Leftasty. Builds and shows window.
 */
public class Main {

    public static void main(String[] args) {
        // must run before any Swing components are created
        Theme.install();

        // not thread safe, all UI work must happen here
        SwingUtilities.invokeLater(() -> {
            final JFrame application = new AppBuilder()
                    .addPantryView()
                    .addAddIngredientView()
                    .addCreateProfileView()
                    .addSidebarView()
                    .addRecipesPlaceholderView()
                    .addPantryUseCases()
                    .addSearchIngredientsUseCase()
                    .addCreateProfileUseCase()
                    .build();

            application.setSize(1100, 720);
            application.setMinimumSize(new Dimension(800, 520));
            application.setLocationRelativeTo(null); // centre on screen
            application.setVisible(true);
        });

        Ingredient egg = new Ingredient("egg");
        ArrayList<Ingredient> ingredientlist = new ArrayList<>();
        ingredientlist.add(egg);
        ApiCall.MakeAPICall(ingredientlist);
    }
}
