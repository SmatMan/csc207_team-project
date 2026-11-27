package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import interface_adapter.recipe_detail.RecipeDetailState;
import interface_adapter.recipe_detail.RecipeDetailViewModel;
import interface_adapter.recipe_detail.RecipeDetailViewModelListener;
import view.components.IngredientIcons;
import view.components.ScrollablePanel;
import view.components.SectionBox;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The sidebar while a recipe is open: the ingredients it uses that are in the pantry,
 * and the ones still needed, each with its quantity.
 */
public class RecipeIngredientsPanel extends JPanel implements RecipeDetailViewModelListener {

    private final RecipeDetailViewModel recipeDetailViewModel;
    private final IngredientIcons ingredientIcons;
    private final JPanel list = new ScrollablePanel();

    public RecipeIngredientsPanel(RecipeDetailViewModel recipeDetailViewModel, IngredientIcons ingredientIcons) {
        super(new BorderLayout());
        this.recipeDetailViewModel = recipeDetailViewModel;
        this.recipeDetailViewModel.addListener(this);
        this.ingredientIcons = ingredientIcons;
        setOpaque(false);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        final JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void stateChanged() {
        final RecipeDetailState state = recipeDetailViewModel.getState();
        list.removeAll();
        if (state.getError() == null) {
            final JLabel heading = new JLabel("Ingredients");
            heading.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_HEADING));
            heading.setForeground(Theme.TEXT);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(heading);
            list.add(Box.createVerticalStrut(Theme.GAP_M));
            addSection("You have", state.getHave(), false);
            addSection("You'll need", state.getNeed(), true);
        }
        list.revalidate();
        list.repaint();
    }

    private void addSection(String title, List<RecipeDetailState.Line> lines, boolean missing) {
        if (lines.isEmpty()) {
            return;
        }
        final JPanel section = SectionBox.create(title);
        for (RecipeDetailState.Line line : lines) {
            section.add(row(line, missing));
        }
        SectionBox.fitHeight(section);
        list.add(section);
        list.add(Box.createVerticalStrut(Theme.GAP_S));
    }

    /**
     * One ingredient: icon tile, name, and quantity on the right. Missing ones are greyed.
     */
    private JPanel row(RecipeDetailState.Line line, boolean missing) {
        final JLabel name = new JLabel(line.getName());
        name.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
        name.setForeground(missing ? Theme.TEXT_MUTED : Theme.TEXT);

        final JLabel quantity = new JLabel(line.getQuantity(), SwingConstants.RIGHT);
        quantity.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_CAPTION + 1));
        quantity.setForeground(Theme.TEXT_MUTED);

        final JPanel row = new JPanel(new BorderLayout(Theme.GAP_M, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS + 2, Theme.GAP_XS, Theme.GAP_S));
        row.add(ingredientIcons.tile(line.getName()), BorderLayout.WEST);
        row.add(name, BorderLayout.CENTER);
        row.add(quantity, BorderLayout.EAST);
        // The recipe's own wording, e.g. "1/2 red onion, sliced thinly", on hover.
        if (!line.getOriginal().isBlank()) {
            row.setToolTipText(line.getOriginal());
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }
}
