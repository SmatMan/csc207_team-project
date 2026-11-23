package view.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import view.theme.Theme;

/**
 * A list row that highlights while the mouse is over it and only then shows
 * one of its children (e.g. a remove button).
 */
public class HoverRow extends JPanel {

    private final Component revealOnHover;
    private boolean hovered;

    /**
     * Creates the row.
     * @param layout the row's layout
     * @param revealOnHover a child that is hidden until the mouse is over the row
     */
    public HoverRow(LayoutManager layout, Component revealOnHover) {
        super(layout);
        this.revealOnHover = revealOnHover;
        setOpaque(false);
        revealOnHover.setVisible(false);

        final MouseAdapter tracker = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                // Moving onto the revealed child also "exits" the row, so check where the mouse really is.
                final Point point = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), HoverRow.this);
                if (!contains(point)) {
                    setHovered(false);
                }
            }
        };
        addMouseListener(tracker);
        revealOnHover.addMouseListener(tracker);
    }

    private void setHovered(boolean hovered) {
        if (this.hovered != hovered) {
            this.hovered = hovered;
            revealOnHover.setVisible(hovered);
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovered) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.NEUTRAL_TINT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.ARC_TILE + 4, Theme.ARC_TILE + 4);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
