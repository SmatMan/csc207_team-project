package view.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JComponent;

import view.theme.Theme;

/**
 * A rounded, tinted square with an icon centred on it.
 */
public class IngredientTile extends JComponent {

    public static final int SIZE = 36;

    private final Color tint;
    private final Icon icon;

    public IngredientTile(Color tint, Icon icon) {
        this.tint = tint;
        this.icon = icon;
        final Dimension size = new Dimension(SIZE, SIZE);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Layouts may make this taller than it is wide; keep the tile square and centred.
        final int x = (getWidth() - SIZE) / 2;
        final int y = (getHeight() - SIZE) / 2;
        g2.setColor(tint);
        g2.fillRoundRect(x, y, SIZE, SIZE, Theme.ARC_TILE, Theme.ARC_TILE);
        icon.paintIcon(this, g2, x + (SIZE - icon.getIconWidth()) / 2, y + (SIZE - icon.getIconHeight()) / 2);
        g2.dispose();
    }
}
