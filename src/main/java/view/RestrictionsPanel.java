package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import interface_adapter.restrictions.RestrictionsController;
import interface_adapter.restrictions.RestrictionsState;
import interface_adapter.restrictions.RestrictionsViewModel;
import interface_adapter.restrictions.RestrictionsViewModelListener;
import view.components.WrapLayout;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The Restrictions tab: diet preset chips, then one boxed checklist per category.
 * Ticked means allowed; unticked items are greyed out and struck through.
 */
public class RestrictionsPanel extends JPanel implements RestrictionsViewModelListener {

    private final RestrictionsViewModel restrictionsViewModel;
    private RestrictionsController restrictionsController;

    private final JPanel content = new ScrollablePanel();
    private final JLabel errorLabel = new JLabel();

    // Widgets by name, so state changes can update them in place.
    private final Map<String, List<JCheckBox>> checkBoxes = new HashMap<>();
    private final Map<String, JToggleButton> presetChips = new HashMap<>();
    private final Map<String, JLabel> categoryHeaders = new HashMap<>();

    // What the widgets were built from; rebuilt only if this changes.
    private Map<String, List<String>> builtCategories = new LinkedHashMap<>();
    private List<String> builtPresets = new ArrayList<>();

    private final Font allowedFont = Theme.font(Weight.REGULAR, Theme.SIZE_BODY);
    private final Font excludedFont;

    public RestrictionsPanel(RestrictionsViewModel restrictionsViewModel) {
        super(new BorderLayout(0, Theme.GAP_S));
        this.restrictionsViewModel = restrictionsViewModel;
        this.restrictionsViewModel.addListener(this);
        setOpaque(false);

        final Map<TextAttribute, Object> strikethrough = new HashMap<>();
        strikethrough.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        excludedFont = allowedFont.deriveFont(strikethrough);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        final JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        errorLabel.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        errorLabel.setForeground(Theme.PRIMARY_HOVER);
        errorLabel.setVisible(false);

        add(scrollPane, BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
    }

    public void setRestrictionsController(RestrictionsController restrictionsController) {
        this.restrictionsController = restrictionsController;
    }

    @Override
    public void stateChanged() {
        final RestrictionsState state = restrictionsViewModel.getState();
        if (!state.getIngredientsByCategory().equals(builtCategories)
                || !state.getPresetNames().equals(builtPresets)) {
            rebuild(state);
        }
        update(state);
    }

    private void rebuild(RestrictionsState state) {
        content.removeAll();
        checkBoxes.clear();
        presetChips.clear();
        categoryHeaders.clear();

        content.add(caption("DIET PRESETS"));
        content.add(Box.createVerticalStrut(Theme.GAP_XS));
        content.add(presetChips(state.getPresetNames()));
        content.add(Box.createVerticalStrut(Theme.GAP_L));
        content.add(caption("FOODS  ·  untick what you don't eat"));
        content.add(Box.createVerticalStrut(Theme.GAP_S));

        for (Map.Entry<String, List<String>> category : state.getIngredientsByCategory().entrySet()) {
            content.add(categoryBox(category.getKey(), category.getValue()));
            content.add(Box.createVerticalStrut(Theme.GAP_S));
        }

        builtCategories = new LinkedHashMap<>(state.getIngredientsByCategory());
        builtPresets = new ArrayList<>(state.getPresetNames());
        content.revalidate();
        content.repaint();
    }

    private JPanel presetChips(List<String> presetNames) {
        final JPanel chips = new JPanel(new WrapLayout(FlowLayout.LEFT, Theme.GAP_XS + 2, Theme.GAP_XS + 2));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String presetName : presetNames) {
            final JToggleButton chip = Styles.clickable(new JToggleButton(presetName));
            // The toggle has already flipped when the action fires, so isSelected() is the new choice.
            chip.addActionListener(evt -> restrictionsController.setPresetActive(presetName, chip.isSelected()));
            presetChips.put(presetName, chip);
            chips.add(chip);
        }
        return chips;
    }

    private JPanel categoryBox(String category, List<String> ingredients) {
        final JPanel box = Styles.card(new JPanel());
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JLabel header = caption(category.toUpperCase());
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP_XS, 0));
        categoryHeaders.put(category, header);
        box.add(header);

        for (String ingredient : ingredients) {
            final JCheckBox check = Styles.clickable(new JCheckBox(ingredient));
            check.setOpaque(false);
            check.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Ticked means allowed, so unticking excludes the ingredient.
            check.addActionListener(evt -> restrictionsController.setExcluded(ingredient, !check.isSelected()));
            checkBoxes.computeIfAbsent(ingredient, key -> new ArrayList<>()).add(check);
            box.add(check);
        }

        // Stop BoxLayout stretching the card taller than its contents.
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getPreferredSize().height));
        return box;
    }

    private void update(RestrictionsState state) {
        for (Map.Entry<String, List<JCheckBox>> entry : checkBoxes.entrySet()) {
            final boolean allowed = !state.getExcludedIngredients().contains(entry.getKey());
            for (JCheckBox check : entry.getValue()) {
                check.setSelected(allowed);
                check.setFont(allowed ? allowedFont : excludedFont);
                check.setForeground(allowed ? Theme.TEXT : Theme.TEXT_MUTED);
            }
        }

        for (Map.Entry<String, List<String>> category : state.getIngredientsByCategory().entrySet()) {
            int excluded = 0;
            for (String ingredient : category.getValue()) {
                if (state.getExcludedIngredients().contains(ingredient)) {
                    excluded++;
                }
            }
            String text = category.getKey().toUpperCase();
            if (excluded > 0) {
                text += "  ·  " + excluded + " EXCLUDED";
            }
            categoryHeaders.get(category.getKey()).setText(text);
        }

        for (Map.Entry<String, JToggleButton> entry : presetChips.entrySet()) {
            final boolean modified = state.getModifiedPresets().contains(entry.getKey());
            final JToggleButton chip = entry.getValue();
            chip.setSelected(state.getActivePresets().contains(entry.getKey()));
            // An active preset you've changed by hand turns sunset instead of delft.
            Styles.chip(chip, modified ? Theme.HIGHLIGHT : Theme.SELECTED, modified ? Theme.TEXT : Color.WHITE);
            chip.setToolTipText(modified ? "Changed by hand" : null);
        }

        errorLabel.setText(state.getError());
        errorLabel.setVisible(state.getError() != null);
    }

    private static JLabel caption(String text) {
        final JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        label.setForeground(Theme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * A panel that always matches the scroll pane's width, so content wraps
     * instead of scrolling sideways.
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height - 32;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
