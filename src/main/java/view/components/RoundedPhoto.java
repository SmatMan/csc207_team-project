package view.components;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import view.theme.Theme;

/**
 * A photo downloaded in the background, scaled to cover its area (cropping any overflow)
 * with rounded corners. It's prepared once per size and screen scale, so repainting
 * (e.g. while scrolling) is a plain copy. Shows a plain tint until the photo arrives.
 */
public class RoundedPhoto extends JComponent {

    private final int arc;
    private final boolean topCornersOnly;
    private BufferedImage image;
    private BufferedImage prepared;
    private int preparedWidth;
    private int preparedHeight;

    /**
     * Creates the photo.
     * @param url the photo's address ("" for none: just the tint)
     * @param preferredSize the size it would like
     * @param arc the corner rounding, as a diameter in pixels
     * @param topCornersOnly true to round only the top corners (e.g. a photo on top of a card)
     */
    public RoundedPhoto(String url, Dimension preferredSize, int arc, boolean topCornersOnly) {
        this.arc = arc;
        this.topCornersOnly = topCornersOnly;
        setPreferredSize(preferredSize);
        RemoteImages.load(url, loaded -> {
            image = loaded;
            prepared = null;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        // Work in real screen pixels (e.g. 1.5x on a 150% display) so the copy is 1:1.
        final double screenScale = g2.getTransform().getScaleX();
        final int width = (int) Math.ceil(getWidth() * screenScale);
        final int height = (int) Math.ceil(getHeight() * screenScale);
        if (prepared == null || width != preparedWidth || height != preparedHeight) {
            prepared = prepare(width, height, screenScale);
            preparedWidth = width;
            preparedHeight = height;
        }
        g2.drawImage(prepared, 0, 0, getWidth(), getHeight(), null);
        g2.dispose();
    }

    private BufferedImage prepare(int width, int height, double screenScale) {
        final BufferedImage result = new BufferedImage(Math.max(1, width), Math.max(1, height),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // The shape first (rounded, optionally with square bottom corners)...
        final float scaledArc = (float) (arc * screenScale);
        final Area shape = new Area(new RoundRectangle2D.Float(0, 0, width, height, scaledArc, scaledArc));
        if (topCornersOnly) {
            shape.add(new Area(new Rectangle2D.Float(0, height / 2f, width, height / 2f)));
        }
        g2.setColor(Theme.NEUTRAL_TINT);
        g2.fill(shape);

        // ...then the photo painted only where the shape is, keeping its smooth corners.
        if (image != null) {
            g2.setComposite(AlphaComposite.SrcIn);
            final double scale = Math.max((double) width / image.getWidth(), (double) height / image.getHeight());
            final int scaledWidth = (int) Math.ceil(image.getWidth() * scale);
            final int scaledHeight = (int) Math.ceil(image.getHeight() * scale);
            g2.drawImage(image, (width - scaledWidth) / 2, (height - scaledHeight) / 2, scaledWidth, scaledHeight, null);
        }
        g2.dispose();
        return result;
    }
}
