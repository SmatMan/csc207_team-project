package view.components;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Small interface icons (star, health, timer, servings) from icons/ui, and recipe category
 * icons from icons/categories, drawn in any colour.
 * They come from the same Fluent Emoji High Contrast set as the ingredient icons.
 */
public final class UiIcons {

    public static final String STAR = "star";
    public static final String HEALTH = "seedling";
    public static final String TIMER = "stopwatch";
    public static final String SERVINGS = "fork-and-knife-with-plate";
    public static final String BACK = "left-arrow";

    private static final Map<String, Icon> CACHE = new HashMap<>();

    private UiIcons() {
    }

    /**
     * Returns an interface icon at the given size, recoloured to a single colour.
     * @param name the icon, e.g. {@link #STAR}
     * @param size the width and height in pixels
     * @param color the colour to draw it in
     * @return the icon
     */
    public static Icon get(String name, int size, Color color) {
        return load("icons/ui/", name, size, color);
    }

    /**
     * Returns a recipe category icon (from icons/categories), e.g. "pizza".
     * @param name the icon's file name without ".svg"
     * @param size the width and height in pixels
     * @param color the colour to draw it in
     * @return the icon
     */
    public static Icon category(String name, int size, Color color) {
        return load("icons/categories/", name, size, color);
    }

    private static Icon load(String folder, String name, int size, Color color) {
        return CACHE.computeIfAbsent(folder + name + "/" + size + "/" + color.getRGB(), key -> {
            final FlatSVGIcon icon = new FlatSVGIcon(folder + name + ".svg", size, size);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(original -> color));
            return new CachedIcon(icon);
        });
    }
}
