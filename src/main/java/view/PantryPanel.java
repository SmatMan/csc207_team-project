package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import interface_adapter.pantry.PantryController;
import interface_adapter.pantry.PantryState;
import interface_adapter.pantry.PantryViewModel;
import interface_adapter.pantry.PantryViewModelListener;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The Ingredients tab: the ingredients in the user's pantry, each with a remove button,
 * or a hint to add some when the pantry is empty.
 */
public class PantryPanel extends JPanel implements PantryViewModelListener {

    private static final String EMPTY_CARD = "empty";
    private static final String LIST_CARD = "list";

    private final PantryViewModel pantryViewModel;
    private PantryController pantryController;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JPanel list = Styles.card(new JPanel());
    private final JLabel errorLabel = new JLabel();

    public PantryPanel(PantryViewModel pantryViewModel) {
        super(new BorderLayout(0, Theme.GAP_S));
        this.pantryViewModel = pantryViewModel;
        this.pantryViewModel.addListener(this);
        setOpaque(false);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        // Wrap the card so it hugs its rows instead of stretching to fill the tab.
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
     * Asks the user for an ingredient name and passes it on to be added.
     */
    public void promptAddIngredient() {
        final String name = JOptionPane.showInputDialog(this, "Ingredient name:", "Add ingredient",
                JOptionPane.PLAIN_MESSAGE);
        // null means the user cancelled; anything else, even blank, is for the use case to judge.
        if (name != null) {
            pantryController.addIngredient(name);
        }
    }

    @Override
    public void stateChanged() {
        final PantryState state = pantryViewModel.getState();

        list.removeAll();
        for (String ingredient : state.getIngredients()) {
            list.add(row(ingredient));
        }
        list.revalidate();
        list.repaint();
        cardLayout.show(cards, state.getIngredients().isEmpty() ? EMPTY_CARD : LIST_CARD);

        errorLabel.setText(state.getError());
        errorLabel.setVisible(state.getError() != null);
    }

    private JPanel row(String ingredient) {
        final JLabel name = new JLabel(ingredient);
        name.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_BODY));
        name.setForeground(Theme.TEXT);

        final JButton remove = Styles.clickable(new JButton("×"));
        remove.putClientProperty("JButton.buttonType", "toolBarButton");
        remove.setForeground(Theme.TEXT_MUTED);
        remove.setToolTipText("Remove " + ingredient);
        remove.addActionListener(evt -> pantryController.removeIngredient(ingredient));

        final JPanel row = new JPanel(new BorderLayout(Theme.GAP_S, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
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
