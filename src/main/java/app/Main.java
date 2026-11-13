package app;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
                    .addSidebarView()
                    .addRecipesPlaceholderView()
                    .build();

            application.setSize(1100, 720);
            application.setMinimumSize(new Dimension(800, 520));
            application.setLocationRelativeTo(null); // centre on screen
            application.setVisible(true);
        });
    }
}
