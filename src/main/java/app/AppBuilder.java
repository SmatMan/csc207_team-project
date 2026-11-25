package app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import data_access.FileCreateProfileDataAccessObject;
import data_access.FileIngredientCorpusDataAccessObject;
import data_access.FileIntoleranceCatalogue;
import data_access.FilePantryDataAccessObject;
import entity.KnownIngredient;
import interface_adapter.ViewManagerModel;
import interface_adapter.create_profile.CreateProfileController;
import interface_adapter.create_profile.CreateProfilePresenter;
import interface_adapter.create_profile.CreateProfileViewModel;
import interface_adapter.pantry.PantryController;
import interface_adapter.pantry.PantryPresenter;
import interface_adapter.pantry.PantryViewModel;
import interface_adapter.search_ingredients.SearchIngredientsController;
import interface_adapter.search_ingredients.SearchIngredientsPresenter;
import interface_adapter.search_ingredients.SearchIngredientsViewModel;
import use_case.add_ingredient.AddIngredientInputBoundary;
import use_case.add_ingredient.AddIngredientInteractor;
import use_case.create_profile.CreateProfileInputBoundary;
import use_case.create_profile.CreateProfileInteractor;
import use_case.create_profile.IntoleranceCatalogue;
import use_case.load_pantry.LoadPantryInputBoundary;
import use_case.load_pantry.LoadPantryInteractor;
import use_case.remove_ingredient.RemoveIngredientInputBoundary;
import use_case.remove_ingredient.RemoveIngredientInteractor;
import use_case.search_ingredients.SearchIngredientsInputBoundary;
import use_case.search_ingredients.SearchIngredientsInteractor;
import use_case.search_ingredients.SearchIngredientsOutputBoundary;
import view.AddIngredientPanel;
import view.CreateProfilePanel;
import view.PantryPanel;
import view.SidebarView;
import view.ViewManager;
import view.components.IngredientIcons;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * Builds the application: creates the views and wires each use case's
 * controller, interactor and presenter together.
 */
public class AppBuilder {

    // Temporary main-area card until the recipe views exist.
    private static final String RECIPES_PLACEHOLDER = "recipes placeholder";

    private static final String LOGO_RESOURCE = "/logo.png";

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    // Intolerance catalogue bundled with the app (a placeholder until the API's ingredient
    // list is available); created profiles are saved to profiles.json.
    final IntoleranceCatalogue intoleranceCatalogue = new FileIntoleranceCatalogue("/restrictions.json");
    final FileCreateProfileDataAccessObject createProfileDataAccessObject =
            new FileCreateProfileDataAccessObject("profiles.json");

    // The user's pantry is saved to pantry.json.
    final FilePantryDataAccessObject pantryDataAccessObject = new FilePantryDataAccessObject("pantry.json");

    // Every ingredient the app knows about (a placeholder until the API's ingredient list is available).
    final FileIngredientCorpusDataAccessObject ingredientCorpusDataAccessObject =
            new FileIngredientCorpusDataAccessObject("/ingredients.json");

    // How ingredients are drawn, shared by the pantry list and the add-ingredient search.
    private final IngredientIcons ingredientIcons = buildIngredientIcons();

    private PantryViewModel pantryViewModel;
    private PantryPanel pantryPanel;
    private CreateProfileViewModel createProfileViewModel;
    private CreateProfilePanel createProfilePanel;
    private SearchIngredientsViewModel searchIngredientsViewModel;
    private AddIngredientPanel addIngredientPanel;
    private SidebarView sidebarView;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
        cardPanel.setBackground(Theme.BACKGROUND);
    }

    /**
     * Adds the Ingredients tab. Must be called before {@link #addSidebarView()}.
     * @return this builder
     */
    public AppBuilder addPantryView() {
        pantryViewModel = new PantryViewModel();
        pantryPanel = new PantryPanel(pantryViewModel);
        pantryPanel.setIngredientIcons(ingredientIcons);
        return this;
    }

    /**
     * Adds the add-ingredient button and search. Must be called after {@link #addPantryView()}
     * and before {@link #addSidebarView()}.
     * @return this builder
     */
    public AppBuilder addAddIngredientView() {
        searchIngredientsViewModel = new SearchIngredientsViewModel();
        addIngredientPanel = new AddIngredientPanel(searchIngredientsViewModel, pantryViewModel, ingredientIcons);
        return this;
    }

    /**
     * Adds the Search Ingredients Use Case, used by the add-ingredient search.
     * @return this builder
     */
    public AppBuilder addSearchIngredientsUseCase() {
        final SearchIngredientsOutputBoundary searchIngredientsPresenter =
                new SearchIngredientsPresenter(searchIngredientsViewModel);
        final SearchIngredientsInputBoundary searchIngredientsInteractor =
                new SearchIngredientsInteractor(ingredientCorpusDataAccessObject, searchIngredientsPresenter);

        final SearchIngredientsController searchIngredientsController =
                new SearchIngredientsController(searchIngredientsInteractor);
        addIngredientPanel.setSearchIngredientsController(searchIngredientsController);
        return this;
    }

    /**
     * Builds the icon lookup for ingredient tiles. The view only gets plain name-to-category
     * maps from the corpus, so it never depends on entities or data access.
     * @return the icon lookup
     */
    private IngredientIcons buildIngredientIcons() {
        final Map<String, String> canonicalByTerm = new HashMap<>();
        final Map<String, String> categoryByName = new HashMap<>();
        for (KnownIngredient known : ingredientCorpusDataAccessObject.getKnownIngredients()) {
            categoryByName.put(known.getName(), known.getCategory());
            canonicalByTerm.put(known.getName().toLowerCase(Locale.ROOT), known.getName());
            for (String alias : known.getAliases()) {
                canonicalByTerm.putIfAbsent(alias.toLowerCase(Locale.ROOT), known.getName());
            }
        }
        return new IngredientIcons("/icons/ingredient-icons.json", canonicalByTerm, categoryByName);
    }

    /**
     * Adds the Profile tab. Must be called before {@link #addSidebarView()}.
     * @return this builder
     */
    public AppBuilder addCreateProfileView() {
        createProfileViewModel = new CreateProfileViewModel();
        createProfilePanel = new CreateProfilePanel(createProfileViewModel);
        return this;
    }

    /**
     * Adds the sidebar (Ingredients and Profile tabs, plus the add-ingredient search
     * pinned at the bottom) to the application.
     * @return this builder
     */
    public AppBuilder addSidebarView() {
        sidebarView = new SidebarView(pantryPanel, createProfilePanel, addIngredientPanel);
        return this;
    }

    /**
     * Adds the pantry use cases (load pantry, add ingredient and remove ingredient),
     * then loads the saved pantry into the Ingredients tab.
     * @return this builder
     */
    public AppBuilder addPantryUseCases() {
        final PantryPresenter pantryPresenter = new PantryPresenter(pantryViewModel);

        final LoadPantryInputBoundary loadPantryInteractor =
                new LoadPantryInteractor(pantryDataAccessObject, pantryPresenter);
        final AddIngredientInputBoundary addIngredientInteractor =
                new AddIngredientInteractor(pantryDataAccessObject, pantryPresenter);
        final RemoveIngredientInputBoundary removeIngredientInteractor =
                new RemoveIngredientInteractor(pantryDataAccessObject, pantryPresenter);

        final PantryController pantryController = new PantryController(
                loadPantryInteractor, addIngredientInteractor, removeIngredientInteractor);
        pantryPanel.setPantryController(pantryController);
        addIngredientPanel.setPantryController(pantryController);

        pantryController.loadPantry();
        return this;
    }

    /**
     * Adds the create profile use case to the Profile tab.
     * @return this builder
     */
    public AppBuilder addCreateProfileUseCase() {
        final CreateProfilePresenter createProfilePresenter = new CreateProfilePresenter(createProfileViewModel);

        final CreateProfileInputBoundary createProfileInteractor =
                new CreateProfileInteractor(intoleranceCatalogue, createProfileDataAccessObject, createProfilePresenter);

        final CreateProfileController createProfileController =
                new CreateProfileController(createProfileInteractor, intoleranceCatalogue);
        createProfilePanel.setCreateProfileController(createProfileController);
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
        setAppIcon(application);

        final JPanel root = new JPanel(new BorderLayout());
        root.add(sidebarView, BorderLayout.WEST);
        root.add(cardPanel, BorderLayout.CENTER);
        application.setContentPane(root);

        viewManagerModel.setState(RECIPES_PLACEHOLDER);
        viewManagerModel.notifyViewChanged();

        return application;
    }

    /**
     * Shows the logo in the window's title bar or taskbar, and in the Dock on macOS.
     * @param application the application window
     * @throws RuntimeException if the logo resource is missing
     */
    private static void setAppIcon(JFrame application) {
        final URL logoUrl = AppBuilder.class.getResource(LOGO_RESOURCE);
        if (logoUrl == null) {
            throw new RuntimeException("Missing resource: " + LOGO_RESOURCE);
        }
        final Image logo = new ImageIcon(logoUrl).getImage();
        application.setIconImage(logo);

        // The window icon isn't used for the Dock on macOS, so set it there too where supported.
        if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
            Taskbar.getTaskbar().setIconImage(logo);
        }
    }
}
