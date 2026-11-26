package view.components;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

import org.json.JSONArray;
import org.json.JSONObject;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import view.theme.Theme;

/**
 * Decides how an ingredient looks: its icon and the tint of the tile behind it.
 * Icons come from a JSON mapping (ingredient name to icon, with a fallback icon and a
 * tint per category); every icon is drawn in the theme's text colour.
 */
public class IngredientIcons {

    public static final int ICON_SIZE = 20;

    /** The section for ingredients that aren't in the corpus. */
    public static final String OTHER = "Other";

    private static final String ICON_FOLDER = "icons/ingredients/";

    // Lowercased names and aliases -> canonical name, so "tomato" finds Tomatoes.
    private final Map<String, String> canonicalByTerm;
    private final Map<String, String> categoryByName;

    private final List<String> categoryOrder = new ArrayList<>();
    private final Map<String, String> iconByIngredient = new HashMap<>();
    private final Map<String, String> iconByCategory = new HashMap<>();
    private final Map<String, Color> tintByCategory = new HashMap<>();
    private final String defaultIcon;
    private final Color defaultTint;
    private final Map<String, Icon> iconCache = new HashMap<>();

    /**
     * Creates the icon lookup.
     * @param mappingResource classpath path of the icon mapping, e.g. "/icons/ingredient-icons.json"
     * @param canonicalByTerm lowercased ingredient names and aliases, mapped to canonical names
     * @param categoryByName canonical ingredient names, mapped to their categories
     * @throws RuntimeException if the mapping is missing or invalid
     */
    public IngredientIcons(String mappingResource, Map<String, String> canonicalByTerm,
                           Map<String, String> categoryByName) {
        this.canonicalByTerm = new HashMap<>(canonicalByTerm);
        this.categoryByName = new HashMap<>(categoryByName);

        final JSONObject mapping = readResource(mappingResource);
        final JSONObject fallback = mapping.getJSONObject("default");
        defaultIcon = fallback.getString("icon");
        defaultTint = tint(fallback.getString("tint"));

        final JSONArray order = mapping.getJSONArray("order");
        for (int i = 0; i < order.length(); i++) {
            categoryOrder.add(order.getString(i));
        }

        final JSONObject categories = mapping.getJSONObject("categories");
        for (String category : categories.keySet()) {
            final JSONObject look = categories.getJSONObject(category);
            iconByCategory.put(category, look.getString("icon"));
            tintByCategory.put(category, tint(look.getString("tint")));
        }
        final JSONObject ingredients = mapping.getJSONObject("ingredients");
        for (String ingredient : ingredients.keySet()) {
            iconByIngredient.put(ingredient, ingredients.getString(ingredient));
        }
    }

    /**
     * Returns the category of an ingredient, matching names and aliases case-insensitively.
     * @param ingredient the ingredient name as shown in the pantry
     * @return the category, or null if the ingredient isn't known
     */
    public String categoryOf(String ingredient) {
        return categoryByName.get(canonical(ingredient));
    }

    /**
     * Groups ingredients into their categories for display. Categories follow the mapping's
     * order (any others come after, alphabetically, then {@link #OTHER} for unknown
     * ingredients), and ingredients are alphabetical within each category.
     * @param ingredients the ingredient names, in any order
     * @return category name to its ingredients, in display order; empty categories are left out
     */
    public Map<String, List<String>> groupByCategory(List<String> ingredients) {
        final Map<String, List<String>> groups = new HashMap<>();
        for (String ingredient : ingredients) {
            final String category = categoryOf(ingredient);
            groups.computeIfAbsent(category == null ? OTHER : category, key -> new ArrayList<>()).add(ingredient);
        }

        final List<String> categories = new ArrayList<>(groups.keySet());
        categories.sort(Comparator.comparingInt(this::rank).thenComparing(String.CASE_INSENSITIVE_ORDER));

        final Map<String, List<String>> ordered = new LinkedHashMap<>();
        for (String category : categories) {
            final List<String> members = groups.get(category);
            members.sort(String.CASE_INSENSITIVE_ORDER);
            ordered.put(category, members);
        }
        return ordered;
    }

    /**
     * Where a category goes: its place in the mapping's order, then unlisted ones, then Other.
     */
    private int rank(String category) {
        if (OTHER.equals(category)) {
            return Integer.MAX_VALUE;
        }
        final int index = categoryOrder.indexOf(category);
        return index >= 0 ? index : categoryOrder.size();
    }

    /**
     * Returns the tint for an ingredient's tile, chosen by its category.
     * @param ingredient the ingredient name
     * @return the tile colour
     */
    public Color tintOf(String ingredient) {
        return tintByCategory.getOrDefault(categoryOf(ingredient), defaultTint);
    }

    /**
     * Returns an ingredient's icon: its own, else its category's, else a generic one.
     * @param ingredient the ingredient name
     * @return the icon, {@link #ICON_SIZE} pixels square
     */
    public Icon iconOf(String ingredient) {
        String icon = iconByIngredient.get(canonical(ingredient));
        if (icon == null) {
            icon = iconByCategory.getOrDefault(categoryOf(ingredient), defaultIcon);
        }
        return iconCache.computeIfAbsent(icon, IngredientIcons::loadIcon);
    }

    /**
     * Creates a tile showing an ingredient's icon on its category's tint.
     * @param ingredient the ingredient name
     * @return the tile
     */
    public IngredientTile tile(String ingredient) {
        return new IngredientTile(tintOf(ingredient), iconOf(ingredient));
    }

    private String canonical(String ingredient) {
        return canonicalByTerm.getOrDefault(ingredient.trim().toLowerCase(Locale.ROOT), ingredient.trim());
    }

    private static Icon loadIcon(String name) {
        final FlatSVGIcon icon = new FlatSVGIcon(ICON_FOLDER + name + ".svg", ICON_SIZE, ICON_SIZE);
        // The icons are single-colour, so recolour every pixel to the text colour.
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.TEXT));
        return new CachedIcon(icon);
    }

    private static Color tint(String name) {
        switch (name) {
            case "sage":
                return Theme.SUCCESS_TINT;
            case "terracotta":
                return Theme.PRIMARY_TINT;
            case "sunset":
                return Theme.HIGHLIGHT_TINT;
            case "delft":
                return Theme.DELFT_TINT;
            case "eggshell":
                return Theme.EGGSHELL;
            default:
                return Theme.NEUTRAL_TINT;
        }
    }

    private static JSONObject readResource(String resource) {
        try (InputStream in = IngredientIcons.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new RuntimeException("Missing resource: " + resource);
            }
            return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
