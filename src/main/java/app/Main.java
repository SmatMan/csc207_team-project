package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import view.MainView;
import API.ApiCall;
import Class.Ingredient;
import java.util.List;

/**
 * Entry point of the Recipe Manager. Builds and shows window.
 */
public class Main {

    public static void main(String[] args) {
        // not thread safe, all UI work must happen here
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Recipe Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MainView());
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null); // centre on screen
            frame.setVisible(true);
        });
        Ingredient apple = new Ingredient("Apple");
        ApiCall.MakeAPICall(List.of(apple));

    }
}
