package app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import interface_adapter.ViewManagerModel;
import view.SidebarView;
import view.ViewManager;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * Builds the application: creates the views and wires each use case's
 * controller, interactor and presenter together.
 */
public class AppBuilder {

    // Temporary main-area card until the recipe views exist.
    private static final String RECIPES_PLACEHOLDER = "recipes placeholder";

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private SidebarView sidebarView;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
        cardPanel.setBackground(Theme.BACKGROUND);
    }

    /**
     * Adds the sidebar (Ingredients and Restrictions tabs) to the application.
     * @return this builder
     */
    public AppBuilder addSidebarView() {
        sidebarView = new SidebarView();
        return this;
    }

    /**
     * Adds a placeholder for the main area. Remove once the recipe views are added.
     * @return this builder
     */
    public AppBuilder addRecipesPlaceholderView() {
        final JLabel label = new JLabel("Recipes will appear here");
        label.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_HEADING));
        label.setForeground(Theme.TEXT_MUTED);

        final JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setOpaque(false);
        placeholder.add(label);
        cardPanel.add(placeholder, RECIPES_PLACEHOLDER);
        return this;
    }

    /**
     * Creates the application window: the sidebar on the left and the
     * main view area filling the rest.
     * @return the application window
     */
    public JFrame build() {
        final JFrame application = new JFrame("Leftasty");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel root = new JPanel(new BorderLayout());
        root.add(sidebarView, BorderLayout.WEST);
        root.add(cardPanel, BorderLayout.CENTER);
        application.setContentPane(root);

        viewManagerModel.setState(RECIPES_PLACEHOLDER);
        viewManagerModel.notifyViewChanged();

        return application;
    }
}
