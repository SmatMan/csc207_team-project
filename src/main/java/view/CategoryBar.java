package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import view.components.IngredientTile;
import view.components.UiIcons;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * "Explore categories": a row of dish types and a row of cuisines. One of each can be
 * selected at a time; clicking a selected chip clears it.
 */
public class CategoryBar extends JPanel {

    /**
     * Told whenever the selected categories change.
     */
    public interface CategoryListener {

        /**
         * The selection changed.
         * @param dishType the selected dish type (as the recipe service names it), or null for any
         * @param cuisine the selected cuisine, or null for any
         */
        void categoriesChanged(String dishType, String cuisine);
    }

    // Label shown, value the recipe service expects (Spoonacular's "type" and "cuisine"), icon.
    private static final String[][] DISH_TYPES = {
        {"Main course", "main course", "shallow-pan-of-food"},
        {"Side dish", "side dish", "falafel"},
        {"Breakfast", "breakfast", "pancakes"},
        {"Soup", "soup", "pot-of-food"},
        {"Salad", "salad", "green-salad"},
        {"Appetizer", "appetizer", "stuffed-flatbread"},
        {"Dessert", "dessert", "shortcake"},
        {"Snack", "snack", "popcorn"},
        {"Drink", "drink", "tropical-drink"},
    };
    private static final String[][] CUISINES = {
        {"Italian", "Italian", "pizza"},
        {"Mexican", "Mexican", "taco"},
        {"Chinese", "Chinese", "takeout-box"},
        {"Indian", "Indian", "curry-rice"},
        {"Japanese", "Japanese", "sushi"},
        {"Thai", "Thai", "steaming-bowl"},
        {"Mediterranean", "Mediterranean", "olive"},
        {"American", "American", "hamburger"},
        {"French", "French", "croissant"},
    };

    // Each chip's colour: a pastel for its icon tile (and its fill when selected), and a
    // deeper shade of the same hue for its outline when selected. Palette colours first,
    // then pastels that sit well beside them.
    private static final Color[][] COLORS = {
        {new Color(0xFBE3DA), Theme.TERRACOTTA},
        {new Color(0xE2EFE7), Theme.SAGE},
        {new Color(0xFBEED7), Theme.HIGHLIGHT_DEEP},
        {new Color(0xE3E5EF), Theme.DELFT},
        {new Color(0xF8DFE6), new Color(0xD97A93)},
        {new Color(0xDDEAF4), new Color(0x6A9BC3)},
        {new Color(0xFAF3D1), new Color(0xD9B648)},
        {new Color(0xE9E3F3), new Color(0x8E7CC3)},
        {new Color(0xD9F0EC), new Color(0x4FA89A)},
    };
    // Cuisines start further along the colours, so a chip never matches the one above it.
    private static final int CUISINE_COLOR_OFFSET = 4;

    private static final int ICON_SIZE = 20;

    private final List<Chip> dishTypeChips = new ArrayList<>();
    private final List<Chip> cuisineChips = new ArrayList<>();
    private String dishType;
    private String cuisine;
    private CategoryListener listener;

    public CategoryBar() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JLabel heading = new JLabel("Explore categories");
        heading.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_HEADING));
        heading.setForeground(Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(heading);
        add(Box.createVerticalStrut(Theme.GAP_S));
        add(row(DISH_TYPES, dishTypeChips, true));
        add(Box.createVerticalStrut(Theme.GAP_XS));
        add(row(CUISINES, cuisineChips, false));
    }

    public void setCategoryListener(CategoryListener listener) {
        this.listener = listener;
    }

    public String getDishType() {
        return dishType;
    }

    public String getCuisine() {
        return cuisine;
    }

    /**
     * Describes the selected categories for display, e.g. "Main course, Italian".
     * @return the selected categories' names, or null if none are selected
     */
    public String describeSelection() {
        final List<String> names = new ArrayList<>();
        for (Chip chip : dishTypeChips) {
            if (chip.selected) {
                names.add(chip.label);
            }
        }
        for (Chip chip : cuisineChips) {
            if (chip.selected) {
                names.add(chip.label);
            }
        }
        return names.isEmpty() ? null : String.join(", ", names);
    }

    private JScrollPane row(String[][] categories, List<Chip> chips, boolean isDishType) {
        final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_S, Theme.GAP_XS));
        row.setOpaque(false);
        // Cancel FlowLayout's leading gap so the first chip lines up with the heading.
        row.setBorder(BorderFactory.createEmptyBorder(0, -Theme.GAP_S, 0, 0));
        for (int i = 0; i < categories.length; i++) {
            final String[] category = categories[i];
            final Color[] colors = COLORS[(i + (isDishType ? 0 : CUISINE_COLOR_OFFSET)) % COLORS.length];
            final Chip chip = new Chip(category[0], category[1], category[2], isDishType, colors[0], colors[1]);
            chips.add(chip);
            row.add(chip);
        }

        final JScrollPane scrollPane = new JScrollPane(row,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        // The mouse wheel scrolls the row sideways, since it can't scroll up or down.
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.addMouseWheelListener(new SmoothSideScroll(scrollPane.getHorizontalScrollBar()));
        final int height = row.getPreferredSize().height + scrollPane.getHorizontalScrollBar().getPreferredSize().height;
        scrollPane.setPreferredSize(new Dimension(0, height));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return scrollPane;
    }

    private void toggle(Chip chip) {
        if (chip.isDishType) {
            dishType = chip.value.equals(dishType) ? null : chip.value;
        }
        else {
            cuisine = chip.value.equals(cuisine) ? null : chip.value;
        }
        for (Chip each : dishTypeChips) {
            each.setSelected(each.value.equals(dishType));
        }
        for (Chip each : cuisineChips) {
            each.setSelected(each.value.equals(cuisine));
        }
        if (listener != null) {
            listener.categoriesChanged(dishType, cuisine);
        }
    }

    /**
     * Turns mouse-wheel (or touchpad) movement into a smooth sideways glide: each movement
     * moves a target position, and the row eases towards it over a few frames, instead of
     * jumping a fixed step per notch.
     */
    private static final class SmoothSideScroll implements MouseWheelListener {

        // How far one wheel notch moves the row, in pixels.
        private static final int PIXELS_PER_NOTCH = 60;
        // Each frame covers this share of the remaining distance (ease out).
        private static final double EASE = 0.25;
        private static final int FRAME_MS = 12;

        private final JScrollBar bar;
        private final Timer timer = new Timer(FRAME_MS, evt -> step());
        private double position;
        private double target;

        private SmoothSideScroll(JScrollBar bar) {
            this.bar = bar;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
            if (!timer.isRunning()) {
                // Start from wherever the row is now (it may have been dragged meanwhile).
                position = bar.getValue();
                target = position;
            }
            // Precise rotation keeps touchpads smooth: they report fractions of a notch.
            final double max = bar.getMaximum() - bar.getVisibleAmount();
            target = Math.max(0, Math.min(max, target + evt.getPreciseWheelRotation() * PIXELS_PER_NOTCH));
            timer.start();
        }

        private void step() {
            position += (target - position) * EASE;
            if (Math.abs(target - position) < 0.5) {
                position = target;
                timer.stop();
            }
            bar.setValue((int) Math.round(position));
        }
    }

    /**
     * One category: an icon tile in the category's colour and a label on a rounded card.
     * When selected, the card fills with the colour and is outlined in a deeper shade of it.
     */
    private final class Chip extends JPanel {

        private final String label;
        private final String value;
        private final boolean isDishType;
        private final Color tint;
        private final Color strong;
        private boolean selected;
        private boolean hovered;

        private Chip(String label, String value, String icon, boolean isDishType, Color tint, Color strong) {
            super(new BorderLayout(Theme.GAP_S, 0));
            this.label = label;
            this.value = value;
            this.isDishType = isDishType;
            this.tint = tint;
            this.strong = strong;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_XS, Theme.GAP_M));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            final JLabel text = new JLabel(label);
            text.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
            text.setForeground(Theme.TEXT);

            add(new IngredientTile(tint, UiIcons.category(icon, ICON_SIZE, Theme.TEXT)), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    toggle(Chip.this);
                }

                @Override
                public void mouseEntered(MouseEvent evt) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent evt) {
                    hovered = false;
                    repaint();
                }
            });
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int arc = Theme.ARC_TILE + 4;
            g2.setColor(selected ? tint : Theme.SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (selected) {
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(strong);
            }
            else {
                g2.setColor(hovered ? strong : Theme.BORDER);
            }
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
