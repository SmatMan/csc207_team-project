package view.components;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import view.theme.Theme;

/**
 * Read-only HTML text that wraps to the width it's given (Swing's editor panes otherwise
 * report a single long line) and opens links in the web browser.
 */
public class WrappingHtml extends JEditorPane {

    // Used before the first layout, when there's no width to wrap to yet.
    private static final int FALLBACK_WIDTH = 600;

    private int measuredWidth = -1;

    /**
     * Creates the text.
     * @param html the HTML to show (without the surrounding html tag)
     * @param font the font for the text
     * @param color the text colour
     */
    public WrappingHtml(String html, Font font, Color color) {
        super("text/html", "");
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setMargin(new Insets(0, 0, 0, 0));
        // Use the component's font and colour rather than the HTML default.
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(font);
        setForeground(color);
        // Otherwise setting the text moves the caret, and the scroll pane scrolls to follow it.
        ((DefaultCaret) getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        setText("<html><body style='color:" + Theme.hex(color) + "'>" + html + "</body></html>");
        // When the layout gives us a different width than we measured for, measure again.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                if (getWidth() != measuredWidth) {
                    SwingUtilities.invokeLater(WrappingHtml.this::revalidate);
                }
            }
        });
        addHyperlinkListener(evt -> {
            if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED && evt.getURL() != null
                    && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(evt.getURL().toURI());
                }
                catch (IOException | URISyntaxException ex) {
                    // Couldn't open the browser; the link text is still visible.
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        // Wrap to our real width once laid out; before that, guess from the parent's width.
        int width = getWidth();
        if (width <= 0 && getParent() != null) {
            width = getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right;
        }
        if (width <= 0) {
            width = FALLBACK_WIDTH;
        }
        measuredWidth = width;
        setSize(width, Short.MAX_VALUE);
        return new Dimension(width, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
