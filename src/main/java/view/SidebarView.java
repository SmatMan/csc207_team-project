package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The left sidebar: a switch between the Ingredients and Profile tabs,
 * the content of the selected tab, and the Add ingredient button pinned at the bottom.
 */
public class SidebarView extends JPanel {

    public static final int SIDEBAR_WIDTH = 300;

    private static final String INGREDIENTS_TAB = "ingredients";
    private static final String PROFILE_TAB = "profile";

    private final CardLayout tabLayout = new CardLayout();
    private final JPanel tabContent = new JPanel(tabLayout);
    private final JButton addIngredientButton;

    /**
     * Creates the sidebar.
     * @param pantryPanel the content of the Ingredients tab
     * @param createProfilePanel the content of the Profile tab
     */
    public SidebarView(PantryPanel pantryPanel, CreateProfilePanel createProfilePanel) {
        super(new BorderLayout(0, Theme.GAP_L));
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                BorderFactory.createEmptyBorder(Theme.GAP_XL, Theme.GAP_L, Theme.GAP_L, Theme.GAP_L)));
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        tabContent.setOpaque(false);
        tabContent.add(pantryPanel, INGREDIENTS_TAB);
        tabContent.add(createProfilePanel, PROFILE_TAB);

        addIngredientButton = Styles.clickable(Styles.primaryButton(new JButton("+  Add ingredient")));
        addIngredientButton.addActionListener(evt -> pantryPanel.promptAddIngredient());

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
        final JToggleButton profile = Styles.clickable(Styles.segment(new JToggleButton("Profile")));

        final ButtonGroup group = new ButtonGroup();
        group.add(ingredients);
        group.add(profile);

        ingredients.addActionListener(evt -> showTab(INGREDIENTS_TAB));
        profile.addActionListener(evt -> showTab(PROFILE_TAB));

        final JPanel track = Styles.segmentTrack(new JPanel(new GridLayout(1, 2, Theme.GAP_XS, 0)));
        track.setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS));
        track.add(ingredients);
        track.add(profile);
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, track.getPreferredSize().height));
        return track;
    }

    private void showTab(String tab) {
        tabLayout.show(tabContent, tab);
        // Adding ingredients only makes sense on the Ingredients tab.
        addIngredientButton.setVisible(INGREDIENTS_TAB.equals(tab));
    }
}
