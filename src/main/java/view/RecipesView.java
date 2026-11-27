package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import interface_adapter.pantry.PantryViewModel;
import interface_adapter.recipe_detail.RecipeDetailController;
import interface_adapter.recipes.RecipesController;
import interface_adapter.recipes.RecipesState;
import interface_adapter.recipes.RecipesViewModel;
import interface_adapter.recipes.RecipesViewModelListener;
import view.components.FloatingHeaderPanel;
import view.components.RecipeHtml;
import view.components.RoundedPhoto;
import view.components.ScrollablePanel;
import view.components.UiIcons;
import view.components.WrapLayout;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The main area: recipes that use what's in the pantry, as a grid of cards, fewest
 * missing ingredients first. Searches again by itself shortly after the pantry changes,
 * and loads more recipes when scrolled to the bottom.
 */
public class RecipesView extends JPanel implements RecipesViewModelListener {

    // Wait this long after the last pantry change, so several quick edits make one search.
    private static final int SEARCH_DELAY_MS = 1000;
    // Start loading the next page when this close to the bottom.
    private static final int LOAD_MORE_DISTANCE = 200;
    // How far to scroll before the categories hide or come back, so small jiggles don't toggle them.
    private static final int SCROLL_THRESHOLD = 12;
    // How long the mouse rests on a card before its preview appears, and the preview's size.
    private static final int PREVIEW_DELAY_MS = 450;
    // Roughly how wide the preview should be; its text wraps to fit.
    private static final int PREVIEW_WIDTH = 300;
    private static final int PREVIEW_STEPS = 3;

    private static final int CARD_WIDTH = 228;
    private static final int IMAGE_HEIGHT = 150;
    private static final int CARD_PADDING = 14;
    private static final int BASE_TEXT_HEIGHT = 128;
    private static final int DETAILS_ROW_HEIGHT = 24;

    private static final String GRID_CARD = "grid";
    private static final String MESSAGE_CARD = "message";

    /** What the view is waiting for, so results and errors land in the right place. */
    private enum Pending { NOTHING, FIRST_PAGE, NEXT_PAGE }

    private final RecipesViewModel recipesViewModel;
    private final PantryViewModel pantryViewModel;
    private RecipesController recipesController;
    private RecipeDetailController recipeDetailController;

    // Hover preview: shown after the mouse rests on a card for a moment.
    private final Timer previewTimer = new Timer(PREVIEW_DELAY_MS, evt -> showPreview());
    private JComponent previewCard;
    private RecipesState.RecipeCard previewRecipe;
    private JPanel preview;

    private final Timer searchTimer = new Timer(SEARCH_DELAY_MS, evt -> search());
    // Searches go over the network, so run them off the Swing thread, one at a time, in order.
    private final ExecutorService searchThread = Executors.newSingleThreadExecutor(task -> {
        final Thread thread = new Thread(task, "recipe-search");
        thread.setDaemon(true);
        return thread;
    });
    private List<String> lastSearched;
    private List<String> lastIngredients = new ArrayList<>();
    private Pending pending = Pending.NOTHING;
    private boolean nextPageFailed;

    private final CategoryBar categoryBar = new CategoryBar();
    private final JLabel subtitle = new JLabel(" ");
    // The categories float over the top of the recipes and slide away while scrolling down.
    private final FloatingHeaderPanel floatingHeader;
    private int lastScrollValue;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JScrollPane scrollPane;
    private final JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, Theme.GAP_L, Theme.GAP_L));
    private final JLabel footer = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel messageText = new JLabel("", SwingConstants.CENTER);
    private final JButton retryButton = Styles.clickable(Styles.secondaryButton(new JButton("Try again")));

    /**
     * Creates the recipes view.
     * @param recipesViewModel the view model holding the recipes found
     * @param pantryViewModel the pantry's view model, watched so recipes follow the pantry
     */
    public RecipesView(RecipesViewModel recipesViewModel, PantryViewModel pantryViewModel) {
        super(new BorderLayout());
        this.recipesViewModel = recipesViewModel;
        this.recipesViewModel.addListener(this);
        this.pantryViewModel = pantryViewModel;
        this.pantryViewModel.addListener(this::pantryChanged);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XL + 4, Theme.GAP_XL + 4, 0, Theme.GAP_XL));
        searchTimer.setRepeats(false);
        previewTimer.setRepeats(false);

        grid.setOpaque(false);
        // Cancel FlowLayout's leading gap so cards line up with the heading.
        grid.setBorder(BorderFactory.createEmptyBorder(-Theme.GAP_L, -Theme.GAP_L, 0, 0));

        footer.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION + 1));
        footer.setForeground(Theme.TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_L, 0, Theme.GAP_XL, 0));
        footer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (nextPageFailed) {
                    loadMore();
                }
            }
        });

        cards.setOpaque(false);
        final JPanel header = header();
        floatingHeader = new FloatingHeaderPanel(header, categoryBar, cards);

        final JPanel scrollContent = new ScrollablePanel();
        scrollContent.setLayout(new BorderLayout());
        scrollContent.setOpaque(false);
        // Blank space the height of the header, so the first cards start below it.
        scrollContent.add(floatingHeader.createSpacer(), BorderLayout.NORTH);
        scrollContent.add(grid, BorderLayout.CENTER);
        scrollContent.add(footer, BorderLayout.SOUTH);

        scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(evt -> {
            hidePreview();
            collapseCategoriesOnScroll();
            loadMoreIfNearBottom();
        });

        retryButton.addActionListener(evt -> {
            lastSearched = null;
            search();
        });

        cards.add(scrollPane, GRID_CARD);
        cards.add(messagePanel(), MESSAGE_CARD);
        showMessage("Add ingredients to see recipes", "Recipes appear here as you fill your pantry.", false);

        add(floatingHeader, BorderLayout.CENTER);
    }

    public void setRecipesController(RecipesController recipesController) {
        this.recipesController = recipesController;
    }

    public void setRecipeDetailController(RecipeDetailController recipeDetailController) {
        this.recipeDetailController = recipeDetailController;
    }

    private void openRecipe(int recipeId) {
        if (recipeDetailController != null) {
            recipeDetailController.open(recipeId);
        }
    }

    /**
     * Starts the hover countdown for a card; the preview appears if the mouse stays on it.
     */
    private void showPreviewSoon(JComponent card, RecipesState.RecipeCard recipe) {
        hidePreview();
        if (!recipe.hasPreview()) {
            return;
        }
        previewCard = card;
        previewRecipe = recipe;
        previewTimer.restart();
    }

    private void hidePreview() {
        previewTimer.stop();
        previewCard = null;
        if (preview != null) {
            final Container parent = preview.getParent();
            if (parent != null) {
                final Rectangle area = preview.getBounds();
                parent.remove(preview);
                parent.repaint(area.x, area.y, area.width, area.height);
            }
            preview = null;
        }
    }

    /**
     * Shows the preview beside the hovered card: to its right, or its left if there's no room.
     */
    private void showPreview() {
        if (previewCard == null || SwingUtilities.getRootPane(previewCard) == null) {
            return;
        }
        final JLayeredPane layers = SwingUtilities.getRootPane(previewCard).getLayeredPane();
        preview = buildPreview(previewRecipe);
        final Dimension size = preview.getPreferredSize();
        final Rectangle card = SwingUtilities.convertRectangle(previewCard.getParent(), previewCard.getBounds(), layers);
        final int gap = Theme.GAP_S;
        int x = card.x + card.width + gap;
        if (x + size.width > layers.getWidth() - gap) {
            x = card.x - size.width - gap;
        }
        final int y = Math.max(gap, Math.min(card.y, layers.getHeight() - size.height - gap));
        preview.setBounds(x, y, size.width, size.height);
        layers.add(preview, JLayeredPane.POPUP_LAYER);
        preview.revalidate();
        preview.repaint();
    }

    /**
     * The preview: title, times, the start of the description (keeping its bold), and the first steps.
     */
    private static JPanel buildPreview(RecipesState.RecipeCard recipe) {
        final int textWidth = PREVIEW_WIDTH - 2 * Theme.GAP_L - 8;
        final JPanel panel = Styles.card(new JPanel());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(htmlLabel("<b>" + RecipeHtml.escape(recipe.getTitle()) + "</b>", textWidth,
                Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY + 1), Theme.TEXT));
        panel.add(Box.createVerticalStrut(Theme.GAP_XS));

        final JPanel facts = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        facts.setOpaque(false);
        facts.setAlignmentX(Component.LEFT_ALIGNMENT);
        final Font factFont = Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1);
        if (recipe.getTimeText() != null) {
            facts.add(previewFact(UiIcons.TIMER, "Ready in " + recipe.getTimeText(), factFont));
        }
        if (recipe.getPrepText() != null) {
            facts.add(previewFact(null, "Prep " + recipe.getPrepText(), factFont));
        }
        if (recipe.getServingsText() != null) {
            facts.add(previewFact(UiIcons.SERVINGS, recipe.getServingsText(), factFont));
        }
        panel.add(facts);

        if (!recipe.getSummaryHtml().isBlank()) {
            panel.add(Box.createVerticalStrut(Theme.GAP_S));
            panel.add(htmlLabel(RecipeHtml.firstSentences(recipe.getSummaryHtml(), 2), textWidth,
                    Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1), Theme.TEXT));
        }

        final List<String> steps = recipe.getSteps();
        if (!steps.isEmpty()) {
            panel.add(Box.createVerticalStrut(Theme.GAP_S));
            final StringBuilder list = new StringBuilder("<ol style='margin-left:16px; margin-top:0'>");
            for (int i = 0; i < Math.min(PREVIEW_STEPS, steps.size()); i++) {
                list.append("<li>").append(RecipeHtml.escape(steps.get(i))).append("</li>");
            }
            list.append("</ol>");
            panel.add(htmlLabel(list.toString(), textWidth, Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1),
                    Theme.TEXT));
        }
        final int moreSteps = steps.size() - PREVIEW_STEPS;
        panel.add(Box.createVerticalStrut(Theme.GAP_XS));
        panel.add(htmlLabel(moreSteps > 0 ? "+ " + moreSteps + " more steps  ·  click to open"
                : "Click to open the recipe", textWidth, Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION),
                Theme.TEXT_MUTED));

        // No fixed panel width: Swing's HTML renders fixed-width text a little wider than asked,
        // so the panel sizes itself to its labels and nothing gets cut off.
        return panel;
    }

    private static JLabel htmlLabel(String html, int width, Font font, Color color) {
        // A fixed width makes Swing wrap the HTML onto several lines.
        final JLabel label = new JLabel("<html><div style='width:" + width + "px'>" + html + "</div></html>");
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel previewFact(String icon, String text, Font font) {
        final JLabel label = new JLabel(text);
        if (icon != null) {
            label.setIcon(UiIcons.get(icon, 14, Theme.TEXT_MUTED));
            label.setIconTextGap(Theme.GAP_XS);
        }
        label.setFont(font);
        label.setForeground(Theme.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.GAP_M));
        return label;
    }

    /**
     * Restarts the search countdown when the pantry's ingredients have changed.
     */
    private void pantryChanged() {
        if (!normalized(pantryViewModel.getState().getIngredients()).equals(lastSearched)) {
            subtitle.setText("Updating…");
            searchTimer.restart();
        }
    }

    /**
     * Searches from the first page with the pantry as it is now.
     */
    private void search() {
        if (recipesController == null) {
            return;
        }
        lastIngredients = new ArrayList<>(pantryViewModel.getState().getIngredients());
        lastSearched = normalized(lastIngredients);
        if (!lastIngredients.isEmpty()) {
            subtitle.setText("Finding recipes…");
        }
        pending = Pending.FIRST_PAGE;
        final List<String> ingredients = lastIngredients;
        final String dishType = categoryBar.getDishType();
        final String cuisine = categoryBar.getCuisine();
        searchThread.submit(() -> recipesController.findRecipes(ingredients, dishType, cuisine));
    }

    /**
     * Hides the categories when the user scrolls down, and shows them again when they
     * scroll up or reach the top.
     */
    private void collapseCategoriesOnScroll() {
        // The categories float over the recipes, so hiding them never moves the scroll position.
        final int value = scrollPane.getVerticalScrollBar().getValue();
        final int moved = value - lastScrollValue;
        if (value <= 0) {
            floatingHeader.setCollapsed(false);
        }
        else if (moved > SCROLL_THRESHOLD) {
            floatingHeader.setCollapsed(true);
        }
        else if (moved < -SCROLL_THRESHOLD) {
            floatingHeader.setCollapsed(false);
        }
        if (Math.abs(moved) > SCROLL_THRESHOLD || value <= 0) {
            lastScrollValue = value;
        }
    }

    private void loadMoreIfNearBottom() {
        final RecipesState state = recipesViewModel.getState();
        if (pending != Pending.NOTHING || nextPageFailed || !state.isMoreAvailable() || state.getRecipes().isEmpty()) {
            return;
        }
        // Measure the content itself: the scroll bar's range lags behind until the next layout,
        // which would make freshly added cards look like the bottom and load page after page.
        final JViewport viewport = scrollPane.getViewport();
        final Rectangle visible = viewport.getViewRect();
        final int contentHeight = viewport.getView().getPreferredSize().height;
        if (visible.y + visible.height >= contentHeight - LOAD_MORE_DISTANCE) {
            loadMore();
        }
    }

    /**
     * Asks for the page after the recipes already shown, with the same ingredients and categories.
     */
    private void loadMore() {
        if (recipesController == null || pending != Pending.NOTHING) {
            return;
        }
        nextPageFailed = false;
        pending = Pending.NEXT_PAGE;
        footer.setText("Loading more recipes…");
        footer.setCursor(Cursor.getDefaultCursor());
        final List<String> ingredients = lastIngredients;
        final String dishType = categoryBar.getDishType();
        final String cuisine = categoryBar.getCuisine();
        final int shown = recipesViewModel.getState().getRecipes().size();
        searchThread.submit(() -> recipesController.loadMore(ingredients, dishType, cuisine, shown));
    }

    @Override
    public void stateChanged() {
        // Results arrive on the search thread; Swing must only be touched on its own thread.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::stateChanged);
            return;
        }
        final RecipesState state = recipesViewModel.getState();
        final Pending finished = pending;
        pending = Pending.NOTHING;

        if (state.getError() != null && finished == Pending.NEXT_PAGE) {
            // Keep the recipes already shown; offer to retry just the next page.
            nextPageFailed = true;
            footer.setText("Couldn't load more recipes. Click to try again.");
            footer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        nextPageFailed = false;
        footer.setCursor(Cursor.getDefaultCursor());

        if (state.getError() != null) {
            subtitle.setText(" ");
            showMessage("Couldn't load recipes", state.getError(), true);
        }
        else if (state.getIngredientCount() == 0) {
            subtitle.setText(" ");
            showMessage("Add ingredients to see recipes", "Recipes appear here as you fill your pantry.", false);
        }
        else if (state.getRecipes().isEmpty()) {
            subtitle.setText(basedOn(state.getIngredientCount()));
            final boolean filtered = categoryBar.getDishType() != null || categoryBar.getCuisine() != null;
            showMessage("No recipes found",
                    filtered ? "Try another category." : "Try adding a few more ingredients.", false);
        }
        else {
            subtitle.setText(basedOn(state.getIngredientCount()) + "  ·  fewest missing ingredients first");
            showRecipes(state);
            if (finished == Pending.FIRST_PAGE) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
            // If the cards don't fill the window yet there's nothing to scroll, so keep loading.
            SwingUtilities.invokeLater(this::loadMoreIfNearBottom);
        }
    }

    private void showRecipes(RecipesState state) {
        // Every card the same height, so the grid lines up: make room for details if any recipe has them.
        boolean anyDetails = false;
        for (RecipesState.RecipeCard recipe : state.getRecipes()) {
            anyDetails |= recipe.getTimeText() != null || recipe.getServingsText() != null;
        }
        final int cardHeight = IMAGE_HEIGHT + BASE_TEXT_HEIGHT + (anyDetails ? DETAILS_ROW_HEIGHT : 0);

        grid.removeAll();
        for (RecipesState.RecipeCard recipe : state.getRecipes()) {
            grid.add(new RecipeCardPanel(recipe, cardHeight, this));
        }
        footer.setText(state.isMoreAvailable() ? " " : "That's every recipe for these ingredients.");
        grid.revalidate();
        grid.repaint();
        cardLayout.show(cards, GRID_CARD);
    }

    private JPanel header() {
        categoryBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        // A new category means a new search straight away, from the first page.
        categoryBar.setCategoryListener((dishType, cuisine) -> {
            searchTimer.stop();
            search();
        });

        subtitle.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1));
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Opaque, in the page colour, so recipes scrolling underneath are hidden behind it.
        final JPanel header = new JPanel();
        header.setOpaque(true);
        header.setBackground(Theme.BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP_M, 0));
        header.add(categoryBar);
        header.add(Box.createVerticalStrut(Theme.GAP_S));
        header.add(subtitle);
        return header;
    }

    private JPanel messagePanel() {
        messageTitle.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_HEADING));
        messageTitle.setForeground(Theme.TEXT);
        messageTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageText.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_BODY));
        messageText.setForeground(Theme.TEXT_MUTED);
        messageText.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(messageTitle);
        column.add(Box.createVerticalStrut(Theme.GAP_XS));
        column.add(messageText);
        column.add(Box.createVerticalStrut(Theme.GAP_L));
        column.add(retryButton);

        // GridBagLayout with no constraints centres its single child.
        final JPanel centred = new JPanel(new GridBagLayout());
        centred.setOpaque(false);
        centred.add(column);
        return centred;
    }

    private void showMessage(String title, String text, boolean canRetry) {
        messageTitle.setText(title);
        messageText.setText(text);
        retryButton.setVisible(canRetry);
        cardLayout.show(cards, MESSAGE_CARD);
    }

    /**
     * "Based on 9 ingredients", plus the selected categories if any, so they stay visible
     * while the category bar is collapsed.
     */
    private String basedOn(int ingredientCount) {
        final String selection = categoryBar.describeSelection();
        return "Based on " + ingredientCount + (ingredientCount == 1 ? " ingredient" : " ingredients")
                + (selection == null ? "" : "  ·  " + selection);
    }

    private static List<String> normalized(List<String> ingredients) {
        final List<String> normalized = new ArrayList<>();
        for (String ingredient : ingredients) {
            normalized.add(ingredient.trim().toLowerCase(Locale.ROOT));
        }
        normalized.sort(null);
        return normalized;
    }

    /**
     * One recipe: photo (with its scores on it), title (up to two lines), time and
     * servings, how many of its ingredients you have, and what's missing. Hovering shows
     * a preview; clicking opens the recipe.
     */
    private static final class RecipeCardPanel extends JPanel {

        private static final int TEXT_WIDTH = CARD_WIDTH - 2 * CARD_PADDING;

        private RecipeCardPanel(RecipesState.RecipeCard recipe, int height, RecipesView owner) {
            super(new BorderLayout());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_CARD
                    + "; background: " + Theme.hex(Theme.SURFACE)
                    + "; border: 0,0,0,0," + Theme.hex(Theme.BORDER) + ",1," + Theme.ARC_CARD);

            final JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_M, CARD_PADDING, CARD_PADDING, CARD_PADDING));

            final Font titleFont = Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY + 1);
            for (String line : twoLines(recipe.getTitle(), getFontMetrics(titleFont), TEXT_WIDTH)) {
                text.add(label(line, titleFont, Theme.TEXT));
            }

            final JPanel details = detailsRow(recipe);
            if (details != null) {
                text.add(Box.createVerticalStrut(Theme.GAP_XS));
                text.add(details);
            }

            text.add(Box.createVerticalStrut(Theme.GAP_S));
            text.add(new IngredientBar(recipe.getUsedCount(), recipe.getTotalCount()));
            text.add(Box.createVerticalStrut(Theme.GAP_XS));
            text.add(label(recipe.getUsedCount() + " of " + recipe.getTotalCount() + " ingredients",
                    Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION + 1), Theme.TEXT));

            final String missing = recipe.getMissingIngredients().isEmpty()
                    ? "You have everything"
                    : "Missing: " + String.join(", ", recipe.getMissingIngredients());
            // A plain label shortens itself with "..." when too long; the preview shows the whole recipe.
            text.add(label(missing, Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1), Theme.TEXT_MUTED));

            add(new RecipePhoto(recipe.getImageUrl(), recipe.getRatingText(), recipe.getHealthText()),
                    BorderLayout.NORTH);
            add(text, BorderLayout.CENTER);
            setPreferredSize(new Dimension(CARD_WIDTH, height));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent evt) {
                    owner.showPreviewSoon(RecipeCardPanel.this, recipe);
                }

                @Override
                public void mouseExited(MouseEvent evt) {
                    // Moving onto a child also counts as leaving, so check where the mouse really is.
                    if (!contains(SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), RecipeCardPanel.this))) {
                        owner.hidePreview();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent evt) {
                    owner.hidePreview();
                    owner.openRecipe(recipe.getId());
                }
            });
        }

        /**
         * "⏱ 45 min   🍽 Serves 4", or null if neither is known.
         */
        private static JPanel detailsRow(RecipesState.RecipeCard recipe) {
            if (recipe.getTimeText() == null && recipe.getServingsText() == null) {
                return null;
            }
            final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            final Font font = Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1);
            if (recipe.getTimeText() != null) {
                row.add(iconLabel(UiIcons.TIMER, recipe.getTimeText(), font));
                row.add(Box.createHorizontalStrut(Theme.GAP_M));
            }
            if (recipe.getServingsText() != null) {
                row.add(iconLabel(UiIcons.SERVINGS, recipe.getServingsText(), font));
            }
            row.setMaximumSize(new Dimension(TEXT_WIDTH, row.getPreferredSize().height));
            return row;
        }

        private static JLabel iconLabel(String icon, String text, Font font) {
            final JLabel label = new JLabel(text, UiIcons.get(icon, 14, Theme.TEXT_MUTED), SwingConstants.LEFT);
            label.setIconTextGap(Theme.GAP_XS);
            label.setFont(font);
            label.setForeground(Theme.TEXT_MUTED);
            return label;
        }

        private static JLabel label(String text, Font font, Color color) {
            final JLabel label = new JLabel(text);
            label.setFont(font);
            label.setForeground(color);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setMaximumSize(new Dimension(TEXT_WIDTH, label.getPreferredSize().height));
            return label;
        }

        /**
         * Word-wraps a title onto at most two lines, ending the second with "…" if it doesn't fit.
         */
        private static List<String> twoLines(String title, FontMetrics metrics, int width) {
            final List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            final String[] words = title.split(" ");
            for (int i = 0; i < words.length; i++) {
                final String candidate = line.length() == 0 ? words[i] : line + " " + words[i];
                if (metrics.stringWidth(candidate) <= width || line.length() == 0) {
                    line = new StringBuilder(candidate);
                }
                else if (lines.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(words[i]);
                }
                else {
                    // Out of room on the second line: finish with an ellipsis.
                    String shortened = line.toString();
                    while (!shortened.isEmpty() && metrics.stringWidth(shortened + "…") > width) {
                        shortened = shortened.substring(0, shortened.length() - 1);
                    }
                    lines.add(shortened.trim() + "…");
                    return lines;
                }
            }
            lines.add(line.toString());
            return lines;
        }
    }

    /**
     * The recipe photo, filling the top of the card with its top corners rounded, with a
     * small white pill showing Spoonacular's score and health score when known.
     */
    private static final class RecipePhoto extends RoundedPhoto {

        private static final int ICON_SIZE = 13;

        private final String rating;
        private final String health;

        private RecipePhoto(String url, String rating, String health) {
            super(url, new Dimension(CARD_WIDTH, IMAGE_HEIGHT), Theme.ARC_CARD, true);
            this.rating = rating;
            this.health = health;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintBadge(g2);
            g2.dispose();
        }

        /**
         * "★ 88   🌱 30" (Spoonacular's score, then health score) in a white pill at the top
         * left of the photo.
         */
        private void paintBadge(Graphics2D g2) {
            if (rating == null && health == null) {
                return;
            }
            g2.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_CAPTION + 1));
            final FontMetrics metrics = g2.getFontMetrics();
            final int gap = Theme.GAP_XS;
            final int padding = Theme.GAP_S;

            int width = padding * 2;
            if (rating != null) {
                width += ICON_SIZE + gap + metrics.stringWidth(rating);
            }
            if (health != null) {
                width += (rating != null ? Theme.GAP_M : 0) + ICON_SIZE + gap + metrics.stringWidth(health);
            }
            final int height = 24;
            final int left = 10;
            final int top = 10;
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillRoundRect(left, top, width, height, height, height);

            int x = left + padding;
            if (rating != null) {
                x = paintItem(g2, UiIcons.get(UiIcons.STAR, ICON_SIZE, Theme.HIGHLIGHT_DEEP), rating, x, top, height);
                x += Theme.GAP_M;
            }
            if (health != null) {
                paintItem(g2, UiIcons.get(UiIcons.HEALTH, ICON_SIZE, Theme.SAGE), health, x, top, height);
            }
        }

        /**
         * Paints an icon and its number, returning where the next item can start.
         */
        private int paintItem(Graphics2D g2, Icon icon, String text, int x, int top, int height) {
            final FontMetrics metrics = g2.getFontMetrics();
            icon.paintIcon(this, g2, x, top + (height - ICON_SIZE) / 2);
            final int textX = x + ICON_SIZE + Theme.GAP_XS;
            g2.setColor(Theme.TEXT);
            g2.drawString(text, textX, top + (height + metrics.getAscent() - metrics.getDescent()) / 2);
            return textX + metrics.stringWidth(text);
        }
    }

    /**
     * A thin rounded bar showing what share of a recipe's ingredients are in the pantry.
     */
    private static final class IngredientBar extends JComponent {

        private static final int HEIGHT = 6;

        private final double fraction;

        private IngredientBar(int used, int total) {
            this.fraction = total == 0 ? 0 : (double) used / total;
            setAlignmentX(Component.LEFT_ALIGNMENT);
            final Dimension size = new Dimension(CARD_WIDTH - 2 * CARD_PADDING, HEIGHT);
            setPreferredSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.NEUTRAL_TINT);
            g2.fillRoundRect(0, 0, getWidth(), HEIGHT, HEIGHT, HEIGHT);
            g2.setColor(Theme.SAGE);
            g2.fillRoundRect(0, 0, (int) Math.round(getWidth() * fraction), HEIGHT, HEIGHT, HEIGHT);
            g2.dispose();
        }
    }
}
