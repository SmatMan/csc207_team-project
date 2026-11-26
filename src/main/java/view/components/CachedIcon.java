package view.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 * Wraps an icon that's slow to draw (such as an SVG, which is redrawn from its shapes each
 * time) and keeps a ready-made image of it at the screen's scale, so repainting it, for
 * example while scrolling, is a plain copy.
 */
public class CachedIcon implements Icon {

    private final Icon icon;
    private BufferedImage cached;
    private double cachedScale;

    public CachedIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        final double scale = g instanceof Graphics2D ? ((Graphics2D) g).getTransform().getScaleX() : 1;
        if (cached == null || scale != cachedScale) {
            // Draw once at real screen pixels (e.g. 1.5x on a 150% display) so it stays sharp.
            cached = new BufferedImage((int) Math.ceil(getIconWidth() * scale),
                    (int) Math.ceil(getIconHeight() * scale), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2 = cached.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.scale(scale, scale);
            icon.paintIcon(c, g2, 0, 0);
            g2.dispose();
            cachedScale = scale;
        }
        g.drawImage(cached, x, y, getIconWidth(), getIconHeight(), null);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
