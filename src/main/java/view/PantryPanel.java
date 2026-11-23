package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.icons.FlatClearIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;

import interface_adapter.pantry.PantryController;
import interface_adapter.pantry.PantryState;
import interface_adapter.pantry.PantryViewModel;
import interface_adapter.pantry.PantryViewModelListener;
import view.components.HoverRow;
import view.components.IngredientIcons;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The Ingredients tab: the ingredients in the user's pantry, grouped into category
 * sections (alphabetical within each), each with an icon tile coloured by category and
 * a remove button shown on hover; or a hint to add some when the pantry is empty.
 */
public class PantryPanel extends JPanel implements PantryViewModelListener {

    private static final String EMPTY_CARD = "empty";
    private static final String LIST_CARD = "list";

    private final PantryViewModel pantryViewModel;
    private PantryController pantryController;
    private IngredientIcons ingredientIcons;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JPanel list = new JPanel();
    private final JLabel errorLabel = new JLabel();

    public PantryPanel(PantryViewModel pantryViewModel) {
        super(new BorderLayout(0, Theme.GAP_S));
        this.pantryViewModel = pantryViewModel;
        this.pantryViewModel.addListener(this);
        setOpaque(false);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        // Wrap the sections so they hug their rows instead of stretching to fill the tab.
        final JPanel listHolder = new JPanel(new BorderLayout());
        listHolder.setOpaque(false);
        listHolder.add(list, BorderLayout.NORTH);

        final JScrollPane scrollPane = new JScrollPane(listHolder);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        cards.setOpaque(false);
        cards.add(emptyState("Your pantry is empty", "Add what you have and we'll find recipes."), EMPTY_CARD);
        cards.add(scrollPane, LIST_CARD);

        errorLabel.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        errorLabel.setForeground(Theme.PRIMARY_HOVER);
        errorLabel.setVisible(false);

        add(cards, BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
    }

    public void setPantryController(PantryController pantryController) {
        this.pantryController = pantryController;
    }

    /**
     * Sets how ingredients are drawn (icon tiles and categories). Without it, rows show names only.
     * @param ingredientIcons the icon lookup
     */
    public void setIngredientIcons(IngredientIcons ingredientIcons) {
        this.ingredientIcons = ingredientIcons;
    }

    @Override
    public void stateChanged() {
        final PantryState state = pantryViewModel.getState();

        list.removeAll();
        final Map<String, List<String>> sections;
        if (ingredientIcons == null) {
            sections = Map.of("Ingredients", state.getIngredients());
        }
        else {
            sections = ingredientIcons.groupByCategory(state.getIngredients());
        }
        for (Map.Entry<String, List<String>> section : sections.entrySet()) {
            list.add(section(section.getKey(), section.getValue()));
            list.add(Box.createVerticalStrut(Theme.GAP_S));
        }
        list.revalidate();
        list.repaint();
        cardLayout.show(cards, state.getIngredients().isEmpty() ? EMPTY_CARD : LIST_CARD);

        errorLabel.setText(state.getError());
        errorLabel.setVisible(state.getError() != null);
    }

    /**
     * A rounded box of one category's rows, with the category name set into its top border.
     */
    private JPanel section(String category, List<String> ingredients) {
        final JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        final FlatLineBorder outline = new FlatLineBorder(new Insets(Theme.GAP_XS, Theme.GAP_XS + 2,
                Theme.GAP_S, Theme.GAP_XS + 2), Theme.BORDER, 1f, Theme.ARC_CARD);
        // TitledBorder breaks the line where the title sits, so the name reads "through" the border.
        final TitledBorder titled = BorderFactory.createTitledBorder(outline, " " + category.toUpperCase() + " ",
                TitledBorder.LEADING, TitledBorder.TOP,
                Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION), Theme.TEXT_MUTED);
        section.setBorder(titled);

        for (String ingredient : ingredients) {
            section.add(row(ingredient));
        }
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));
        return section;
    }

    /**
     * A pantry row: the ingredient's icon tile, its name, and a remove button that only
     * appears while the mouse is over the row.
     */
    private JPanel row(String ingredient) {
        final JLabel name = new JLabel(ingredient);
        name.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
        name.setForeground(Theme.TEXT);

        final JButton remove = Styles.clickable(new JButton(new FlatClearIcon()));
        remove.putClientProperty("JButton.buttonType", "toolBarButton");
        remove.setFocusable(false);
        remove.setToolTipText("Remove " + ingredient);
        remove.addActionListener(evt -> pantryController.removeIngredient(ingredient));

        final HoverRow row = new HoverRow(new BorderLayout(Theme.GAP_M, 0), remove);
        row.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS + 2, Theme.GAP_XS + 2,
                Theme.GAP_XS + 2, Theme.GAP_XS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (ingredientIcons != null) {
            row.add(ingredientIcons.tile(ingredient), BorderLayout.WEST);
        }
        row.add(name, BorderLayout.CENTER);
        row.add(remove, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static JPanel emptyState(String title, String message) {
        final JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY));
        titleLabel.setForeground(Theme.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // HTML so long messages wrap within the sidebar.
        final JLabel messageLabel = new JLabel("<html><div style='text-align:center;width:180px'>"
                + message + "</div></html>", SwingConstants.CENTER);
        messageLabel.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1));
        messageLabel.setForeground(Theme.TEXT_MUTED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(titleLabel);
        column.add(Box.createVerticalStrut(Theme.GAP_XS));
        column.add(messageLabel);

        // GridBagLayout with no constraints centres its single child.
        final JPanel centred = new JPanel(new GridBagLayout());
        centred.setOpaque(false);
        centred.add(column);
        return centred;
    }
}
