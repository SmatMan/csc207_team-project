package app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import data_access.FileUserProfileDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.restrictions.RestrictionsController;
import interface_adapter.restrictions.RestrictionsPresenter;
import interface_adapter.restrictions.RestrictionsViewModel;
import use_case.load_profile.LoadProfileInputBoundary;
import use_case.load_profile.LoadProfileInteractor;
import use_case.set_diet_preset.SetDietPresetInputBoundary;
import use_case.set_diet_preset.SetDietPresetInteractor;
import use_case.toggle_restriction.ToggleRestrictionInputBoundary;
import use_case.toggle_restriction.ToggleRestrictionInteractor;
import view.RestrictionsPanel;
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

    // Restriction catalogue bundled with the app (a placeholder until the API's ingredient
    // list is available); the user's choices are saved to profile.json.
    final FileUserProfileDataAccessObject userProfileDataAccessObject =
            new FileUserProfileDataAccessObject("/restrictions.json", "profile.json");

    private RestrictionsViewModel restrictionsViewModel;
    private RestrictionsPanel restrictionsPanel;
    private SidebarView sidebarView;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
        cardPanel.setBackground(Theme.BACKGROUND);
    }

    /**
     * Adds the Restrictions tab. Must be called before {@link #addSidebarView()}.
     * @return this builder
     */
    public AppBuilder addRestrictionsView() {
        restrictionsViewModel = new RestrictionsViewModel();
        restrictionsPanel = new RestrictionsPanel(restrictionsViewModel);
        return this;
    }

    /**
     * Adds the sidebar (Ingredients and Restrictions tabs) to the application.
     * @return this builder
     */
    public AppBuilder addSidebarView() {
        sidebarView = new SidebarView(restrictionsPanel);
        return this;
    }

    /**
     * Adds the dietary profile use cases (load profile, toggle restriction and
     * set diet preset), then loads the saved profile into the Restrictions tab.
     * @return this builder
     */
    public AppBuilder addRestrictionsUseCases() {
        final RestrictionsPresenter restrictionsPresenter = new RestrictionsPresenter(restrictionsViewModel);

        final LoadProfileInputBoundary loadProfileInteractor =
                new LoadProfileInteractor(userProfileDataAccessObject, restrictionsPresenter);
        final ToggleRestrictionInputBoundary toggleRestrictionInteractor =
                new ToggleRestrictionInteractor(userProfileDataAccessObject, restrictionsPresenter);
        final SetDietPresetInputBoundary setDietPresetInteractor =
                new SetDietPresetInteractor(userProfileDataAccessObject, restrictionsPresenter);

        final RestrictionsController restrictionsController = new RestrictionsController(
                loadProfileInteractor, toggleRestrictionInteractor, setDietPresetInteractor);
        restrictionsPanel.setRestrictionsController(restrictionsController);

        restrictionsController.loadProfile();
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
