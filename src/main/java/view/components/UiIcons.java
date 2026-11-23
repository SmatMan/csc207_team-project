package view.components;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Small interface icons (star, heart, timer...) from icons/ui, drawn in any colour.
 * They come from the same Fluent Emoji High Contrast set as the ingredient icons.
 */
public final class UiIcons {

    public static final String STAR = "star";
    public static final String HEART = "red-heart";
    public static final String TIMER = "stopwatch";
    public static final String SERVINGS = "fork-and-knife-with-plate";

    private static final Map<String, Icon> CACHE = new HashMap<>();

    private UiIcons() {
    }

    /**
     * Returns an icon at the given size, recoloured to a single colour.
     * @param name the icon, e.g. {@link #STAR}
     * @param size the width and height in pixels
     * @param color the colour to draw it in
     * @return the icon
     */
    public static Icon get(String name, int size, Color color) {
        return CACHE.computeIfAbsent(name + "/" + size + "/" + color.getRGB(), key -> {
            final FlatSVGIcon icon = new FlatSVGIcon("icons/ui/" + name + ".svg", size, size);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(original -> color));
            return icon;
        });
    }
}
