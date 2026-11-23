package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import entity.Ingredient;
import entity.Intolerance;
import interface_adapter.create_profile.CreateProfileController;
import interface_adapter.create_profile.CreateProfileState;
import interface_adapter.create_profile.CreateProfileViewModel;
import interface_adapter.create_profile.CreateProfileViewModelListener;
import use_case.create_profile.IntoleranceInputData;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The Profile tab. The user types a name, ticks the ingredients to exclude (grouped by
 * category), and presses Create profile. The panel shows the result in a message label
 * under the list.
 */
public class CreateProfilePanel extends JPanel implements CreateProfileViewModelListener {

    private final CreateProfileViewModel createProfileViewModel;
    private CreateProfileController createProfileController;

    private final JTextField nameField = new JTextField();
    private final JLabel categoriesLabel = new JLabel("Tick anything you can't or won't eat");
    private final JPanel categories = Styles.card(new JPanel());
    private final JLabel messageLabel = new JLabel();
    // Remembers every checkbox by category name, so submit() can read them back later.
    private final Map<String, List<JCheckBox>> checkboxesByCategory = new LinkedHashMap<>();

    public CreateProfilePanel(CreateProfileViewModel createProfileViewModel) {
        super(new BorderLayout(0, Theme.GAP_S));
        this.createProfileViewModel = createProfileViewModel;
        // Register as a listener so the presenter can reach us through the view model.
        this.createProfileViewModel.addListener(this);
        setOpaque(false);

        // The category list scrolls vertically. The checkboxes are added later, in buildCategories.
        categories.setLayout(new BoxLayout(categories, BoxLayout.Y_AXIS));

        categoriesLabel.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        categoriesLabel.setForeground(Theme.TEXT_MUTED);

        final JPanel categoriesHolder = new JPanel(new BorderLayout(0, Theme.GAP_XS));
        categoriesHolder.setOpaque(false);
        categoriesHolder.add(categoriesLabel, BorderLayout.NORTH);
        categoriesHolder.add(categories, BorderLayout.CENTER);

        final JScrollPane scrollPane = new JScrollPane(categoriesHolder);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final JButton createButton = Styles.clickable(Styles.primaryButton(new JButton("Create profile")));
        createButton.addActionListener(evt -> submit());

        // The message label stays hidden until the first result comes back.
        messageLabel.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        messageLabel.setForeground(Theme.PRIMARY_HOVER);
        messageLabel.setVisible(false);

        // Layout: name field on top, category list in the middle, message and button at the bottom.
        final JPanel top = new JPanel(new BorderLayout(0, Theme.GAP_XS));
        top.setOpaque(false);
        final JLabel nameLabel = new JLabel("Profile name");
        nameLabel.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        nameLabel.setForeground(Theme.TEXT_MUTED);
        top.add(nameLabel, BorderLayout.NORTH);
        top.add(nameField, BorderLayout.CENTER);

        final JPanel bottom = new JPanel(new BorderLayout(0, Theme.GAP_XS));
        bottom.setOpaque(false);
        bottom.add(messageLabel, BorderLayout.NORTH);
        bottom.add(createButton, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Gives the panel its controller. AppBuilder calls this once, after the controller exists.
     * It also builds the checklist, because the controller is what supplies the categories.
     * @param createProfileController the controller this panel submits to
     */
    public void setCreateProfileController(CreateProfileController createProfileController) {
        this.createProfileController = createProfileController;
        buildCategories(createProfileController.getAvailableIntolerances());
    }

    // Clears the list and adds one section per category.
    private void buildCategories(List<Intolerance> available) {
        categories.removeAll();
        checkboxesByCategory.clear();
        for (Intolerance intolerance : available) {
            categories.add(categorySection(intolerance));
        }
        // Swing needs both calls after the contents change, or the old layout stays on screen.
        categories.revalidate();
        categories.repaint();
    }

    // One section: a bold title, then a checkbox for each ingredient in the category.
    private JPanel categorySection(Intolerance intolerance) {
        final JLabel title = new JLabel(intolerance.getName());
        title.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);

        final List<JCheckBox> checkboxes = new ArrayList<>();
        for (Ingredient ingredient : intolerance.getIngredients()) {
            final JCheckBox checkBox = new JCheckBox(ingredient.getName());
            checkBox.setOpaque(false);
            checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkboxes.add(checkBox);
            section.add(checkBox);
        }
        checkboxesByCategory.put(intolerance.getName(), checkboxes);

        section.add(Box.createVerticalStrut(Theme.GAP_S));
        return section;
    }

    // Runs when the button is pressed. Collects the ticked ingredient names per category
    // and sends them to the controller along with the name field's text.
    private void submit() {
        final List<IntoleranceInputData> intolerances = new ArrayList<>();
        for (Map.Entry<String, List<JCheckBox>> entry : checkboxesByCategory.entrySet()) {
            final List<String> selected = new ArrayList<>();
            for (JCheckBox checkBox : entry.getValue()) {
                if (checkBox.isSelected()) {
                    selected.add(checkBox.getText());
                }
            }
            // A category with nothing ticked is left out of the submission. That is not an error.
            if (!selected.isEmpty()) {
                intolerances.add(new IntoleranceInputData(entry.getKey(), selected));
            }
        }
        createProfileController.createProfile(nameField.getText(), intolerances);
    }

    // Called by the view model after the presenter updates the state.
    // An error shows first. Otherwise a created name shows as a confirmation.
    @Override
    public void stateChanged() {
        final CreateProfileState state = createProfileViewModel.getState();
        if (state.getError() != null) {
            messageLabel.setForeground(Theme.PRIMARY_HOVER);
            messageLabel.setText(state.getError());
            messageLabel.setVisible(true);
        }
        else if (state.getCreatedProfileName() != null) {
            messageLabel.setForeground(Theme.TEXT_MUTED);
            messageLabel.setText("Created \"" + state.getCreatedProfileName() + "\".");
            messageLabel.setVisible(true);
        }
    }
}
