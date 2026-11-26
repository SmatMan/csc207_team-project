package view.components;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A body (e.g. a scrolling list) with a header floating over its top edge. Collapsing
 * slides part of the header up out of sight. The body always keeps its full size, so
 * showing or hiding the header never moves or resizes what's being scrolled.
 *
 * <p>Put {@link #createSpacer()} at the top of the scrolling content so its first row
 * starts below the header instead of behind it.</p>
 */
public class FloatingHeaderPanel extends JPanel {

    private static final int DURATION_MS = 180;
    private static final int FRAME_MS = 15;

    private final JComponent header;
    private final JComponent slidingPart;
    private final JComponent body;
    private final Timer timer = new Timer(FRAME_MS, evt -> step());
    // 0 = header fully shown, 1 = sliding part fully hidden.
    private double hidden;
    private double from;
    private double to;
    private long startTime;

    /**
     * Creates the panel.
     * @param header the header, painted over the top of the body (should be opaque)
     * @param slidingPart the part at the top of the header that slides away when collapsed
     * @param body the content underneath, laid out at full size
     */
    public FloatingHeaderPanel(JComponent header, JComponent slidingPart, JComponent body) {
        super(null);
        this.header = header;
        this.slidingPart = slidingPart;
        this.body = body;
        setOpaque(false);
        // Added first, so it's painted last: on top of the body.
        add(header);
        add(body);
    }

    /**
     * A blank component as tall as the whole header, for the top of the scrolling content.
     * @return the spacer
     */
    public JComponent createSpacer() {
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, header.getPreferredSize().height);
            }
        };
    }

    /**
     * Slides the sliding part of the header out of sight, or back.
     * @param collapsed true to hide it, false to show it
     */
    public void setCollapsed(boolean collapsed) {
        final double target = collapsed ? 1 : 0;
        if (target == to) {
            return;
        }
        from = hidden;
        to = target;
        startTime = System.currentTimeMillis();
        timer.start();
    }

    private void step() {
        final double progress = Math.min(1, (System.currentTimeMillis() - startTime) / (double) DURATION_MS);
        // Ease out: quick at first, settling gently.
        hidden = from + (to - from) * (1 - Math.pow(1 - progress, 3));
        if (progress >= 1) {
            hidden = to;
            timer.stop();
        }
        // Only the header moves; nothing is resized, so no wider re-layout is needed.
        doLayout();
        repaint();
    }

    @Override
    public void doLayout() {
        final int slide = (int) Math.round(slidingPart.getPreferredSize().height * hidden);
        header.setBounds(0, -slide, getWidth(), header.getPreferredSize().height);
        body.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        // Children overlap, so Swing must paint them in order rather than assume they don't.
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return body.getPreferredSize();
    }
}
