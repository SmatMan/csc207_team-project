package view.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * The visual design system for the app: colours, fonts, shapes and spacing.
 * Views should use these constants instead of hardcoding values, so the look
 * can be changed in one place.
 */
public final class Theme {

    // Base palette
    public static final Color EGGSHELL = new Color(0xF4F1DE);
    public static final Color TERRACOTTA = new Color(0xE07A5F);
    public static final Color DELFT = new Color(0x3D405B);
    public static final Color SAGE = new Color(0x81B29A);
    public static final Color SUNSET = new Color(0xF2CC8F);

    // Roles. Use these in views rather than the base palette.
    public static final Color BACKGROUND = new Color(0xFFF8E8);
    public static final Color SURFACE = new Color(0xFFFDF6);
    public static final Color TEXT = DELFT;
    public static final Color TEXT_MUTED = new Color(0x86878F);
    public static final Color BORDER = new Color(0xDEDCCE);
    public static final Color SELECTED = DELFT;

    public static final Color PRIMARY = TERRACOTTA;
    public static final Color PRIMARY_HOVER = new Color(0xC9654B);
    public static final Color PRIMARY_TINT = new Color(0xFAE9DF);

    public static final Color SUCCESS = SAGE;
    public static final Color SUCCESS_TINT = new Color(0xE6EEE4);

    public static final Color HIGHLIGHT = SUNSET;
    public static final Color HIGHLIGHT_TINT = new Color(0xFBEED7);

    public static final Color NEUTRAL_TINT = new Color(0xECEAE6);

    // Corner radii (FlatLaf "arc" is the corner diameter in pixels)
    public static final int ARC_CARD = 20;
    public static final int ARC_TILE = 12;
    public static final int ARC_PILL = 999;

    // Spacing scale
    public static final int GAP_XS = 4;
    public static final int GAP_S = 8;
    public static final int GAP_M = 12;
    public static final int GAP_L = 16;
    public static final int GAP_XL = 24;

    // Type scale
    public static final float SIZE_TITLE = 22f;
    public static final float SIZE_HEADING = 16f;
    public static final float SIZE_BODY = 13f;
    public static final float SIZE_CAPTION = 11f;

    /**
     * The bundled Poppins weights.
     */
    public enum Weight {
        REGULAR("Poppins-Regular.ttf"),
        MEDIUM("Poppins-Medium.ttf"),
        SEMIBOLD("Poppins-SemiBold.ttf"),
        BOLD("Poppins-Bold.ttf");

        private final String fileName;

        Weight(String fileName) {
            this.fileName = fileName;
        }
    }

    private static final Map<Weight, Font> FONTS = new EnumMap<>(Weight.class);

    private Theme() {
    }

    /**
     * Returns the app font in the given weight and size.
     * Falls back to the default sans-serif font if Poppins can't be loaded.
     * @param weight the font weight
     * @param size the point size
     * @return the font
     */
    public static Font font(Weight weight, float size) {
        return FONTS.computeIfAbsent(weight, Theme::loadFont).deriveFont(size);
    }

    /**
     * Installs the FlatLaf look and feel with this theme's colours, font and shapes.
     * Must be called before any Swing components are created.
     */
    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", hex(PRIMARY),
                "@background", hex(BACKGROUND),
                "@foreground", hex(TEXT)));
        FlatLightLaf.setup();

        UIManager.put("defaultFont", font(Weight.REGULAR, SIZE_BODY));

        UIManager.put("Component.arc", ARC_TILE);
        UIManager.put("Button.arc", ARC_PILL);
        UIManager.put("TextComponent.arc", ARC_PILL);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderColor", BORDER);

        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumbArc", ARC_PILL);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.showButtons", false);
    }

    /**
     * Formats a colour as a "#RRGGBB" string, e.g. for FlatLaf style strings.
     * @param color the colour
     * @return the hex string
     */
    public static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    private static Font loadFont(Weight weight) {
        try (InputStream in = Theme.class.getResourceAsStream("/fonts/" + weight.fileName)) {
            if (in == null) {
                return new Font(Font.SANS_SERIF, Font.PLAIN, 1);
            }
            final Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        }
        catch (IOException | FontFormatException ex) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }
    }
}
