package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * The main screen: a tabbed sidebar on the left and the recipe viewer filling the rest.
 */
public class MainView extends JPanel {

    private static final int SIDEBAR_WIDTH = 250;

    public MainView() {
        // BorderLayout: WEST gets its preferred width, CENTER takes all remaining space.
        super(new BorderLayout());

        JTabbedPane sidebar = new JTabbedPane();
        sidebar.addTab("Pantry", new ItemListPanel(
                new String[] {"Eggs", "Flour", "Milk", "Rice", "Tomatoes"}));
        sidebar.addTab("Intolerances", new ItemListPanel(
                new String[] {"Lactose", "Gluten"}));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        // Placeholder until the recipe viewer is built.
        JLabel recipeViewer = new JLabel("Recipes will appear here", SwingConstants.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(recipeViewer, BorderLayout.CENTER);
    }
}
