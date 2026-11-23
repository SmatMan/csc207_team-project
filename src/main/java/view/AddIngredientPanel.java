package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.icons.FlatSearchIcon;

import interface_adapter.pantry.PantryController;
import interface_adapter.pantry.PantryViewModel;
import interface_adapter.search_ingredients.SearchIngredientsController;
import interface_adapter.search_ingredients.SearchIngredientsState;
import interface_adapter.search_ingredients.SearchIngredientsViewModel;
import interface_adapter.search_ingredients.SearchIngredientsViewModelListener;
import view.components.IngredientIcons;
import view.components.MatchHighlighter;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The bottom of the Ingredients tab. Normally an "Add ingredient" button; clicking it turns
 * the button into a search field, with matching ingredients folding out above it.
 * The best match sits at the bottom, next to the field, so Enter adds it.
 */
public class AddIngredientPanel extends JPanel implements SearchIngredientsViewModelListener {

    private static final String BUTTON_CARD = "button";
    private static final String SEARCH_CARD = "search";

    private final SearchIngredientsViewModel searchIngredientsViewModel;
    private final PantryViewModel pantryViewModel;
    private final IngredientIcons ingredientIcons;
    private SearchIngredientsController searchIngredientsController;
    private PantryController pantryController;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JTextField searchField = new JTextField();
    private final JPanel suggestionsCard = Styles.card(new JPanel(new BorderLayout(0, Theme.GAP_XS)));
    private final JPanel suggestionRows = new JPanel();
    private final JLabel messageLabel = new JLabel();

    // Rows top to bottom; the last one is the best match.
    private final List<SuggestionRow> rows = new ArrayList<>();
    private int selected = -1;
    private boolean open;

    /**
     * Creates the add-ingredient bar.
     * @param searchIngredientsViewModel the view model holding the current suggestions
     * @param pantryViewModel the pantry's view model, to mark suggestions already in the pantry
     * @param ingredientIcons how ingredients are drawn
     */
    public AddIngredientPanel(SearchIngredientsViewModel searchIngredientsViewModel,
                              PantryViewModel pantryViewModel, IngredientIcons ingredientIcons) {
        super(new BorderLayout(0, Theme.GAP_S));
        this.searchIngredientsViewModel = searchIngredientsViewModel;
        this.searchIngredientsViewModel.addListener(this);
        this.pantryViewModel = pantryViewModel;
        this.ingredientIcons = ingredientIcons;
        setOpaque(false);

        final JButton addButton = Styles.clickable(Styles.primaryButton(new JButton("+  Add ingredient")));
        addButton.addActionListener(evt -> open());

        searchField.putClientProperty("JTextField.placeholderText", "Search ingredients…");
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_PILL
                + "; margin: 6,8,6,12; focusWidth: 0");
        searchField.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_BODY));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evt) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                search();
            }
        });
        bindKey(KeyEvent.VK_ENTER, "add", this::addSelected);
        bindKey(KeyEvent.VK_ESCAPE, "close", this::close);
        bindKey(KeyEvent.VK_UP, "up", () -> moveSelection(-1));
        bindKey(KeyEvent.VK_DOWN, "down", () -> moveSelection(1));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                // Switching windows is temporary; clicking another control in the app closes the search.
                final Component other = evt.getOppositeComponent();
                if (!evt.isTemporary() && (other == null || !SwingUtilities.isDescendingFrom(other, AddIngredientPanel.this))) {
                    close();
                }
            }
        });

        cards.setOpaque(false);
        cards.add(addButton, BUTTON_CARD);
        cards.add(searchField, SEARCH_CARD);

        suggestionRows.setOpaque(false);
        suggestionRows.setLayout(new BoxLayout(suggestionRows, BoxLayout.Y_AXIS));
        messageLabel.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1));
        messageLabel.setForeground(Theme.TEXT_MUTED);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS, 0));

        suggestionsCard.add(header(), BorderLayout.NORTH);
        suggestionsCard.add(suggestionRows, BorderLayout.CENTER);
        suggestionsCard.add(messageLabel, BorderLayout.SOUTH);
        suggestionsCard.setVisible(false);

        add(suggestionsCard, BorderLayout.CENTER);
        add(cards, BorderLayout.SOUTH);
    }

    public void setSearchIngredientsController(SearchIngredientsController searchIngredientsController) {
        this.searchIngredientsController = searchIngredientsController;
    }

    public void setPantryController(PantryController pantryController) {
        this.pantryController = pantryController;
    }

    /**
     * Swaps the button for the search field and focuses it.
     */
    public void open() {
        open = true;
        searchField.setText("");
        cardLayout.show(cards, SEARCH_CARD);
        searchField.requestFocusInWindow();
        revalidate();
    }

    /**
     * Clears the search, folds the suggestions away and shows the button again.
     */
    public void close() {
        open = false;
        searchField.setText("");
        suggestionsCard.setVisible(false);
        cardLayout.show(cards, BUTTON_CARD);
        revalidate();
        repaint();
    }

    @Override
    public void stateChanged() {
        final SearchIngredientsState state = searchIngredientsViewModel.getState();

        suggestionRows.removeAll();
        rows.clear();
        // Best match first in the state; shown last so it sits right above the field.
        final List<SearchIngredientsState.Suggestion> suggestions = state.getSuggestions();
        for (int i = suggestions.size() - 1; i >= 0; i--) {
            final SuggestionRow row = new SuggestionRow(suggestions.get(i), searchField.getText());
            rows.add(row);
            suggestionRows.add(row);
        }
        // Start on the closest match that isn't already in the pantry, so Enter adds something new.
        selected = rows.size() - 1;
        while (selected > 0 && rows.get(selected).inPantry) {
            selected--;
        }

        messageLabel.setText(state.getError());
        messageLabel.setVisible(state.getError() != null);
        suggestionsCard.setVisible(open && (!rows.isEmpty() || state.getError() != null));

        revalidate();
        repaint();
    }

    private JPanel header() {
        final JLabel hint = new JLabel("Enter adds the closest match");
        hint.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        hint.setForeground(Theme.TEXT_MUTED);

        final JButton closeButton = Styles.clickable(new JButton("×"));
        closeButton.putClientProperty("JButton.buttonType", "toolBarButton");
        closeButton.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_HEADING));
        closeButton.setForeground(Theme.TEXT_MUTED);
        closeButton.setFocusable(false);
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(evt -> close());

        final JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(hint, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);
        return header;
    }

    private void search() {
        if (searchIngredientsController != null) {
            searchIngredientsController.search(searchField.getText());
        }
    }

    private void addSelected() {
        if (selected >= 0 && selected < rows.size()) {
            addIngredient(rows.get(selected).name);
        }
    }

    /**
     * Adds an ingredient to the pantry, then clears the field so another can be added.
     */
    private void addIngredient(String ingredient) {
        pantryController.addIngredient(ingredient);
        searchField.setText("");
        searchField.requestFocusInWindow();
    }

    private void moveSelection(int delta) {
        if (!rows.isEmpty()) {
            selected = Math.max(0, Math.min(rows.size() - 1, selected + delta));
            suggestionRows.repaint();
        }
    }

    private void select(SuggestionRow row) {
        selected = rows.indexOf(row);
        suggestionRows.repaint();
    }

    private boolean inPantry(String ingredient) {
        for (String existing : pantryViewModel.getState().getIngredients()) {
            if (existing.equalsIgnoreCase(ingredient)) {
                return true;
            }
        }
        return false;
    }

    private void bindKey(int keyCode, String name, Runnable action) {
        searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        searchField.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                action.run();
            }
        });
    }

    /**
     * One suggestion: icon tile and name, with what the user typed in bold.
     * Highlighted when selected; clicking adds it.
     */
    private final class SuggestionRow extends JPanel {

        private final String name;
        private final boolean inPantry;

        private SuggestionRow(SearchIngredientsState.Suggestion suggestion, String query) {
            super(new BorderLayout(Theme.GAP_M, 0));
            this.name = suggestion.getName();
            this.inPantry = inPantry(name);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_S));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            final JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            // Glue above and below centres one or two lines against the icon tile.
            text.add(Box.createVerticalGlue());
            if (suggestion.isViaAlias()) {
                // Matched another name: show the ingredient, with what matched underneath in grey.
                text.add(htmlLabel(suggestion.getName(), Theme.SIZE_BODY, Theme.TEXT));
                text.add(htmlLabel(MatchHighlighter.toHtml(suggestion.getMatchedTerm(), query),
                        Theme.SIZE_CAPTION, Theme.TEXT_MUTED));
            }
            else {
                text.add(htmlLabel(MatchHighlighter.toHtml(suggestion.getName(), query), Theme.SIZE_BODY, Theme.TEXT));
            }
            text.add(Box.createVerticalGlue());

            add(ingredientIcons.tile(name), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            if (inPantry) {
                final JLabel tag = new JLabel("in pantry");
                tag.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
                tag.setForeground(Theme.TEXT_MUTED);
                add(tag, BorderLayout.EAST);
            }
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent evt) {
                    select(SuggestionRow.this);
                }

                @Override
                public void mouseClicked(MouseEvent evt) {
                    addIngredient(name);
                }
            });
        }

        private JLabel htmlLabel(String html, float size, Color color) {
            // nobr: one line per label, rather than wrapping and being cut off by the row height.
            final JLabel label = new JLabel("<html><nobr>" + html + "</nobr></html>");
            label.setFont(Theme.font(Weight.REGULAR, size));
            label.setForeground(color);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            return label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (rows.indexOf(this) == selected) {
                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.NEUTRAL_TINT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.ARC_TILE + 4, Theme.ARC_TILE + 4);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
