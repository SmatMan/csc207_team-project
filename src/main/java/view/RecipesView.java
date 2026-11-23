package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import interface_adapter.pantry.PantryViewModel;
import interface_adapter.recipes.RecipesController;
import interface_adapter.recipes.RecipesState;
import interface_adapter.recipes.RecipesViewModel;
import interface_adapter.recipes.RecipesViewModelListener;
import view.components.RemoteImages;
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

    private static final int CARD_WIDTH = 228;
    private static final int IMAGE_HEIGHT = 150;
    private static final int CARD_PADDING = 14;
    private static final int BASE_TEXT_HEIGHT = 128;
    private static final int DETAILS_ROW_HEIGHT = 24;
    private static final int DIETS_ROW_HEIGHT = 30;

    private static final String GRID_CARD = "grid";
    private static final String MESSAGE_CARD = "message";

    /** What the view is waiting for, so results and errors land in the right place. */
    private enum Pending { NOTHING, FIRST_PAGE, NEXT_PAGE }

    private final RecipesViewModel recipesViewModel;
    private final PantryViewModel pantryViewModel;
    private RecipesController recipesController;

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

    private final JLabel subtitle = new JLabel(" ");
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
        super(new BorderLayout(0, Theme.GAP_L));
        this.recipesViewModel = recipesViewModel;
        this.recipesViewModel.addListener(this);
        this.pantryViewModel = pantryViewModel;
        this.pantryViewModel.addListener(this::pantryChanged);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XL + 4, Theme.GAP_XL + 4, 0, Theme.GAP_XL));
        searchTimer.setRepeats(false);

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

        final JPanel scrollContent = new ScrollablePanel();
        scrollContent.setLayout(new BorderLayout());
        scrollContent.setOpaque(false);
        scrollContent.add(grid, BorderLayout.CENTER);
        scrollContent.add(footer, BorderLayout.SOUTH);

        scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(evt -> loadMoreIfNearBottom());

        retryButton.addActionListener(evt -> {
            lastSearched = null;
            search();
        });

        cards.setOpaque(false);
        cards.add(scrollPane, GRID_CARD);
        cards.add(messagePanel(), MESSAGE_CARD);
        showMessage("Add ingredients to see recipes", "Recipes appear here as you fill your pantry.", false);

        add(header(), BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
    }

    public void setRecipesController(RecipesController recipesController) {
        this.recipesController = recipesController;
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
        searchThread.submit(() -> recipesController.findRecipes(ingredients));
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
     * Asks for the page after the recipes already shown, with the same ingredients.
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
        final int shown = recipesViewModel.getState().getRecipes().size();
        searchThread.submit(() -> recipesController.loadMore(ingredients, shown));
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
            showMessage("No recipes found", "Try adding a few more ingredients.", false);
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
        boolean anyDiets = false;
        for (RecipesState.RecipeCard recipe : state.getRecipes()) {
            anyDetails |= recipe.getTimeText() != null || recipe.getServingsText() != null;
            anyDiets |= !recipe.getDiets().isEmpty();
        }
        final int cardHeight = IMAGE_HEIGHT + BASE_TEXT_HEIGHT
                + (anyDetails ? DETAILS_ROW_HEIGHT : 0) + (anyDiets ? DIETS_ROW_HEIGHT : 0);

        grid.removeAll();
        for (RecipesState.RecipeCard recipe : state.getRecipes()) {
            grid.add(new RecipeCardPanel(recipe, cardHeight));
        }
        footer.setText(state.isMoreAvailable() ? " " : "That's every recipe for these ingredients.");
        grid.revalidate();
        grid.repaint();
        cardLayout.show(cards, GRID_CARD);
    }

    private JPanel header() {
        final JLabel title = new JLabel("Recipes for your pantry");
        title.setFont(Theme.font(Weight.BOLD, Theme.SIZE_TITLE));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitle.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_BODY));
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(Theme.GAP_XS));
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

    private static String basedOn(int ingredientCount) {
        return "Based on " + ingredientCount + (ingredientCount == 1 ? " ingredient" : " ingredients");
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
     * One recipe: photo (with rating and likes on it), title (up to two lines), time and
     * servings, how many of its ingredients you have, what's missing, and diet pills.
     */
    private static final class RecipeCardPanel extends JPanel {

        private static final int TEXT_WIDTH = CARD_WIDTH - 2 * CARD_PADDING;
        private static final int MAX_DIETS = 2;

        private RecipeCardPanel(RecipesState.RecipeCard recipe, int height) {
            super(new BorderLayout());
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
            final JLabel missingLabel = label(missing, Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1),
                    Theme.TEXT_MUTED);
            // A plain label shortens itself with "..." when too long; the tooltip shows the whole list.
            missingLabel.setToolTipText(missing);
            text.add(missingLabel);

            if (!recipe.getDiets().isEmpty()) {
                text.add(Box.createVerticalStrut(Theme.GAP_S));
                text.add(dietPills(recipe.getDiets()));
            }

            add(new RecipePhoto(recipe.getImageUrl(), recipe.getRatingText(), recipe.getLikesText()),
                    BorderLayout.NORTH);
            add(text, BorderLayout.CENTER);
            setPreferredSize(new Dimension(CARD_WIDTH, height));
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

        private static JPanel dietPills(List<String> diets) {
            final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_XS, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Cancel FlowLayout's leading gap so the first pill lines up with the text.
            row.setBorder(BorderFactory.createEmptyBorder(0, -Theme.GAP_XS, 0, 0));
            for (int i = 0; i < Math.min(MAX_DIETS, diets.size()); i++) {
                row.add(new Pill(diets.get(i)));
            }
            if (diets.size() > MAX_DIETS) {
                final Pill more = new Pill("+" + (diets.size() - MAX_DIETS));
                more.setToolTipText(String.join(", ", diets));
                row.add(more);
            }
            row.setMaximumSize(new Dimension(TEXT_WIDTH, row.getPreferredSize().height));
            return row;
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
     * small white pill showing the rating and likes when known. Shows a plain tint until
     * the photo has downloaded.
     */
    private static final class RecipePhoto extends JComponent {

        private static final int ICON_SIZE = 13;

        private final String rating;
        private final String likes;
        private BufferedImage image;

        private RecipePhoto(String url, String rating, String likes) {
            this.rating = rating;
            this.likes = likes;
            setPreferredSize(new Dimension(CARD_WIDTH, IMAGE_HEIGHT));
            RemoteImages.load(url, loaded -> {
                image = loaded;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Round the top corners only: a rounded rectangle plus a square block over its bottom half.
            final Area clip = new Area(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    Theme.ARC_CARD, Theme.ARC_CARD));
            clip.add(new Area(new Rectangle2D.Float(0, getHeight() / 2f, getWidth(), getHeight() / 2f)));
            g2.clip(clip);

            if (image == null) {
                g2.setColor(Theme.NEUTRAL_TINT);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            else {
                // Scale to cover the whole area, cropping whatever overflows.
                final double scale = Math.max((double) getWidth() / image.getWidth(),
                        (double) getHeight() / image.getHeight());
                final int width = (int) Math.ceil(image.getWidth() * scale);
                final int height = (int) Math.ceil(image.getHeight() * scale);
                g2.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
            }
            paintBadge(g2);
            g2.dispose();
        }

        /**
         * "★ 3.7   ♥ 1.2k" in a white pill at the top left of the photo.
         */
        private void paintBadge(Graphics2D g2) {
            if (rating == null && likes == null) {
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
            if (likes != null) {
                width += (rating != null ? Theme.GAP_S : 0) + ICON_SIZE + gap + metrics.stringWidth(likes);
            }
            final int height = 24;
            final int left = 10;
            final int top = 10;

            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillRoundRect(left, top, width, height, height, height);

            int x = left + padding;
            final int iconY = top + (height - ICON_SIZE) / 2;
            final int textY = top + (height + metrics.getAscent() - metrics.getDescent()) / 2;
            if (rating != null) {
                x = paintItem(g2, UiIcons.get(UiIcons.STAR, ICON_SIZE, Theme.HIGHLIGHT_DEEP), rating, x, iconY, textY, gap);
                x += Theme.GAP_S;
            }
            if (likes != null) {
                paintItem(g2, UiIcons.get(UiIcons.HEART, ICON_SIZE, Theme.PRIMARY), likes, x, iconY, textY, gap);
            }
        }

        private int paintItem(Graphics2D g2, Icon icon, String text, int x, int iconY, int textY, int gap) {
            icon.paintIcon(this, g2, x, iconY);
            final int textX = x + ICON_SIZE + gap;
            g2.setColor(Theme.TEXT);
            g2.drawString(text, textX, textY);
            return textX + g2.getFontMetrics().stringWidth(text);
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

    /**
     * A small rounded label on a sage tint, for diets such as "Gluten free".
     */
    private static final class Pill extends JLabel {

        private Pill(String text) {
            super(text);
            setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
            setForeground(Theme.TEXT);
            setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.SUCCESS_TINT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
