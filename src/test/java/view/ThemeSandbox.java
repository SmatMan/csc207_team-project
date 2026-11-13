package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * Dev-only window for previewing the theme and base components. Not part of the app.
 * Run with: ./mvnw test-compile exec:java -Dexec.mainClass=view.ThemeSandbox -Dexec.classpathScope=test
 */
public class ThemeSandbox {

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(ThemeSandbox::show);
    }

    private static void show() {
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        content.add(section("Palette", palette()));
        content.add(section("Restrictions (ticked = allowed)", restrictionLists()));
        content.add(section("Chips", chips()));
        content.add(section("Buttons", buttons()));
        content.add(section("Type scale", typeScale()));
        content.add(section("Search field", searchField()));
        content.add(section("Card", card()));

        final JFrame frame = new JFrame("Theme Sandbox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new JScrollPane(content));
        frame.setSize(760, 960);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JComponent palette() {
        final JPanel row = flowRow();
        row.add(swatch("Background", Theme.BACKGROUND));
        row.add(swatch("Eggshell", Theme.EGGSHELL));
        row.add(swatch("Terracotta", Theme.TERRACOTTA));
        row.add(swatch("Delft", Theme.DELFT));
        row.add(swatch("Sage", Theme.SAGE));
        row.add(swatch("Sunset", Theme.SUNSET));
        row.add(swatch("Surface", Theme.SURFACE));
        row.add(swatch("Terracotta tint", Theme.PRIMARY_TINT));
        row.add(swatch("Sage tint", Theme.SUCCESS_TINT));
        row.add(swatch("Sunset tint", Theme.HIGHLIGHT_TINT));
        return row;
    }

    private static JComponent swatch(String name, Color color) {
        final JPanel swatch = new JPanel(new BorderLayout());
        swatch.setOpaque(false);

        final JPanel chip = new JPanel();
        chip.setPreferredSize(new Dimension(64, 44));
        chip.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_TILE
                + "; background: " + Theme.hex(color)
                + "; border: 1,1,1,1," + Theme.hex(Theme.BORDER) + ",1," + Theme.ARC_TILE);

        final JLabel label = new JLabel("<html>" + name + "<br>" + Theme.hex(color) + "</html>");
        label.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION));
        label.setForeground(Theme.TEXT_MUTED);

        swatch.add(chip, BorderLayout.CENTER);
        swatch.add(label, BorderLayout.SOUTH);
        return swatch;
    }

    private static JComponent typeScale() {
        final JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(text("My Pantry", Weight.BOLD, Theme.SIZE_TITLE, Theme.TEXT));
        column.add(text("Dietary restrictions", Weight.SEMIBOLD, Theme.SIZE_HEADING, Theme.TEXT));
        column.add(text("Brown rice, tomatoes, eggs and 4 more", Weight.REGULAR, Theme.SIZE_BODY, Theme.TEXT));
        column.add(text("MEAT  ·  6 SELECTED", Weight.MEDIUM, Theme.SIZE_CAPTION, Theme.TEXT_MUTED));
        return column;
    }

    private static JComponent buttons() {
        final JPanel row = flowRow();

        final JButton primary = new JButton("+  Add ingredient");
        primary.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.PRIMARY)
                + "; hoverBackground: " + Theme.hex(Theme.PRIMARY_HOVER)
                + "; pressedBackground: " + Theme.hex(Theme.PRIMARY_HOVER)
                + "; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0"
                + "; margin: 8,20,8,20");
        primary.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY));

        final JButton secondary = new JButton("Clear all");
        secondary.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; foreground: " + Theme.hex(Theme.TEXT)
                + "; borderColor: " + Theme.hex(Theme.BORDER)
                + "; focusWidth: 0; margin: 8,20,8,20");
        secondary.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));

        row.add(primary);
        row.add(secondary);
        return row;
    }

    private static JComponent chips() {
        final JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        final JPanel presets = flowRow();
        presets.add(chip("Vegan", true, Theme.SELECTED, Color.WHITE));
        presets.add(chip("Pescatarian", false, Theme.SELECTED, Color.WHITE));
        presets.add(chip("Lactose intolerant", false, Theme.SELECTED, Color.WHITE));
        presets.add(chip("Halal", false, Theme.SELECTED, Color.WHITE));

        column.add(caption("Presets (selected = delft)"));
        column.add(presets);
        return column;
    }

    private static JComponent restrictionLists() {
        final JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(restrictionBox("MEAT", new String[] {"Beef", "Chicken", "Pork", "Lamb", "Turkey"},
                new boolean[] {false, false, true, true, true}));
        row.add(Box.createHorizontalStrut(Theme.GAP_M));
        row.add(restrictionBox("DAIRY", new String[] {"Milk", "Cheese", "Butter", "Yogurt"},
                new boolean[] {true, true, true, false}));
        row.add(Box.createHorizontalGlue());
        return row;
    }

    /**
     * A boxed checklist for one restriction category. Ticked = allowed;
     * unticked items are greyed out and struck through.
     */
    private static JComponent restrictionBox(String category, String[] items, boolean[] allowed) {
        final JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentY(Component.TOP_ALIGNMENT);
        box.setMaximumSize(new Dimension(220, Integer.MAX_VALUE));
        box.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_CARD
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; border: 12,16,12,16," + Theme.hex(Theme.BORDER) + ",1," + Theme.ARC_CARD);

        final JLabel header = caption(category);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP_XS, 0));
        box.add(header);

        for (int i = 0; i < items.length; i++) {
            final JCheckBox check = new JCheckBox(items[i], allowed[i]);
            check.setOpaque(false);
            check.setAlignmentX(Component.LEFT_ALIGNMENT);
            check.addItemListener(e -> styleAllowed(check));
            styleAllowed(check);
            box.add(check);
        }
        return box;
    }

    private static void styleAllowed(JCheckBox check) {
        final Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.STRIKETHROUGH,
                check.isSelected() ? Boolean.FALSE : TextAttribute.STRIKETHROUGH_ON);
        check.setFont(Theme.font(Weight.REGULAR, Theme.SIZE_BODY).deriveFont(attributes));
        check.setForeground(check.isSelected() ? Theme.TEXT : Theme.TEXT_MUTED);
    }

    private static JToggleButton chip(String text, boolean selected, Color selectedBg, Color selectedFg) {
        final JToggleButton chip = new JToggleButton(text, selected);
        chip.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; selectedBackground: " + Theme.hex(selectedBg)
                + "; selectedForeground: " + Theme.hex(selectedFg)
                + "; borderColor: " + Theme.hex(Theme.BORDER)
                + "; focusWidth: 0; margin: 4,14,4,14");
        chip.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION + 1));
        return chip;
    }

    private static JComponent searchField() {
        final JTextField field = new JTextField(24);
        field.putClientProperty("JTextField.placeholderText", "Type an ingredient, e.g. brown rice");
        field.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; margin: 6,14,6,14");
        final JPanel row = flowRow();
        row.add(field);
        return row;
    }

    private static JComponent card() {
        final JPanel card = new JPanel(new BorderLayout(Theme.GAP_M, 0));
        card.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_CARD
                + "; background: " + Theme.hex(Theme.SURFACE));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 16));

        final JPanel tile = new JPanel();
        tile.setPreferredSize(new Dimension(40, 40));
        tile.putClientProperty("FlatLaf.style", "arc: " + Theme.ARC_TILE
                + "; background: " + Theme.hex(Theme.HIGHLIGHT_TINT));

        card.add(tile, BorderLayout.WEST);
        card.add(text("Brown rice", Weight.MEDIUM, Theme.SIZE_BODY, Theme.TEXT), BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(280, 64));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel wrapper = flowRow();
        wrapper.add(card);
        return wrapper;
    }

    private static JComponent section(String title, JComponent body) {
        final JPanel section = new JPanel(new BorderLayout(0, Theme.GAP_S));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(text(title, Weight.SEMIBOLD, Theme.SIZE_HEADING, Theme.TEXT), BorderLayout.NORTH);
        section.add(body, BorderLayout.CENTER);
        section.add(Box.createVerticalStrut(Theme.GAP_XL), BorderLayout.SOUTH);
        return section;
    }

    private static JPanel flowRow() {
        final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_S, Theme.GAP_S));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private static JLabel caption(String text) {
        return text(text, Weight.MEDIUM, Theme.SIZE_CAPTION, Theme.TEXT_MUTED);
    }

    private static JLabel text(String text, Weight weight, float size, Color color) {
        final JLabel label = new JLabel(text);
        label.setFont(Theme.font(weight, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
