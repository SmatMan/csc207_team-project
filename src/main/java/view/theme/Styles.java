package view.theme;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import view.theme.Theme.Weight;

/**
 * Applies the shared component styles (via FlatLaf style strings) so views
 * don't repeat them. Each method styles the given component and returns it.
 */
public final class Styles {

    private static final String STYLE = "FlatLaf.style";

    private Styles() {
    }

    /**
     * Filled terracotta pill, for the main action on a screen.
     * @param button the button to style
     * @return the same button
     */
    public static JButton primaryButton(JButton button) {
        button.putClientProperty(STYLE, "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.PRIMARY)
                + "; hoverBackground: " + Theme.hex(Theme.PRIMARY_HOVER)
                + "; pressedBackground: " + Theme.hex(Theme.PRIMARY_HOVER)
                + "; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0"
                + "; margin: 9,20,9,20");
        button.setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY));
        return button;
    }

    /**
     * Outlined pill, for secondary actions.
     * @param button the button to style
     * @return the same button
     */
    public static JButton secondaryButton(JButton button) {
        button.putClientProperty(STYLE, "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; borderColor: " + Theme.hex(Theme.BORDER)
                + "; focusWidth: 0; margin: 9,20,9,20");
        button.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
        return button;
    }

    /**
     * Pill toggle that fills with the given colour when selected.
     * Foreground is left to the theme so selectedForeground applies.
     * @param toggle the toggle to style
     * @param selectedBackground fill colour when selected
     * @param selectedForeground text colour when selected
     * @return the same toggle
     */
    public static JToggleButton chip(JToggleButton toggle, Color selectedBackground, Color selectedForeground) {
        toggle.putClientProperty(STYLE, "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; selectedBackground: " + Theme.hex(selectedBackground)
                + "; selectedForeground: " + Theme.hex(selectedForeground)
                + "; borderColor: " + Theme.hex(Theme.BORDER)
                + "; focusWidth: 0; margin: 4,14,4,14");
        toggle.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION + 1));
        return toggle;
    }

    /**
     * One segment of a segmented control: borderless, filled delft when selected.
     * Place segments inside a {@link #segmentTrack(JComponent)}.
     * @param toggle the toggle to style
     * @return the same toggle
     */
    public static JToggleButton segment(JToggleButton toggle) {
        toggle.putClientProperty(STYLE, "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.NEUTRAL_TINT)
                + "; hoverBackground: " + Theme.hex(Theme.BORDER)
                + "; selectedBackground: " + Theme.hex(Theme.SELECTED)
                + "; selectedForeground: #FFFFFF"
                + "; borderWidth: 0; focusWidth: 0; margin: 6,12,6,12");
        toggle.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
        return toggle;
    }

    /**
     * The rounded track behind a row of segments.
     * @param track the container holding the segments
     * @param <T> the component type
     * @return the same container
     */
    public static <T extends JComponent> T segmentTrack(T track) {
        track.putClientProperty(STYLE, "arc: " + Theme.ARC_PILL
                + "; background: " + Theme.hex(Theme.NEUTRAL_TINT));
        return track;
    }

    /**
     * Rounded surface card with a thin border.
     * @param card the component to style
     * @param <T> the component type
     * @return the same component
     */
    public static <T extends JComponent> T card(T card) {
        card.putClientProperty(STYLE, "arc: " + Theme.ARC_CARD
                + "; background: " + Theme.hex(Theme.SURFACE)
                + "; border: 12,16,12,16," + Theme.hex(Theme.BORDER) + ",1," + Theme.ARC_CARD);
        return card;
    }

    /**
     * Gives a button the hand cursor, so it reads as clickable.
     * @param button the button
     * @param <T> the button type
     * @return the same button
     */
    public static <T extends AbstractButton> T clickable(T button) {
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
