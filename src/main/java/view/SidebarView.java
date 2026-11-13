package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The left sidebar: a switch between the Ingredients and Restrictions tabs,
 * the content of the selected tab, and the Add ingredient button pinned at the bottom.
 */
public class SidebarView extends JPanel {

    public static final int SIDEBAR_WIDTH = 300;

    private static final String INGREDIENTS_TAB = "ingredients";
    private static final String RESTRICTIONS_TAB = "restrictions";

    private final CardLayout tabLayout = new CardLayout();
    private final JPanel tabContent = new JPanel(tabLayout);
    private final JButton addIngredientButton;

    public SidebarView() {
        super(new BorderLayout(0, Theme.GAP_L));
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                BorderFactory.createEmptyBorder(Theme.GAP_XL, Theme.GAP_L, Theme.GAP_L, Theme.GAP_L)));
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        // Placeholders until the real tab panels are built.
        tabContent.setOpaque(false);
        tabContent.add(emptyState("Your pantry is empty",
                "Add what you have and we'll find recipes."), INGREDIENTS_TAB);
        tabContent.add(emptyState("No restrictions set",
                "Pick a diet or untick foods you avoid."), RESTRICTIONS_TAB);

        addIngredientButton = Styles.clickable(Styles.primaryButton(new JButton("+  Add ingredient")));

        add(header(), BorderLayout.NORTH);
        add(tabContent, BorderLayout.CENTER);
        add(addIngredientButton, BorderLayout.SOUTH);
    }

    private JPanel header() {
        final JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        final JLabel title = new JLabel("Leftasty");
        title.setFont(Theme.font(Weight.BOLD, Theme.SIZE_TITLE));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel tabSwitch = tabSwitch();
        tabSwitch.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(Theme.GAP_M));
        header.add(tabSwitch);
        return header;
    }

    private JPanel tabSwitch() {
        final JToggleButton ingredients = Styles.clickable(Styles.segment(new JToggleButton("Ingredients", true)));
        final JToggleButton restrictions = Styles.clickable(Styles.segment(new JToggleButton("Restrictions")));

        final ButtonGroup group = new ButtonGroup();
        group.add(ingredients);
        group.add(restrictions);

        ingredients.addActionListener(evt -> showTab(INGREDIENTS_TAB));
        restrictions.addActionListener(evt -> showTab(RESTRICTIONS_TAB));

        final JPanel track = Styles.segmentTrack(new JPanel(new GridLayout(1, 2, Theme.GAP_XS, 0)));
        track.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS));
        track.add(ingredients);
        track.add(restrictions);
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, track.getPreferredSize().height));
        return track;
    }

    private void showTab(String tab) {
        tabLayout.show(tabContent, tab);
        // Adding ingredients only makes sense on the Ingredients tab.
        addIngredientButton.setVisible(INGREDIENTS_TAB.equals(tab));
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
