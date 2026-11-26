package view.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.ui.FlatLineBorder;

import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * A rounded box of rows with its title set into the top border, e.g. "VEGETABLES".
 */
public final class SectionBox {

    private SectionBox() {
    }

    /**
     * Creates an empty section box; add rows to it, then call {@link #fitHeight(JPanel)}.
     * @param title the section's title (shown in capitals)
     * @return the box, laid out top to bottom
     */
    public static JPanel create(String title) {
        final JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        final FlatLineBorder outline = new FlatLineBorder(new Insets(Theme.GAP_XS, Theme.GAP_XS + 2,
                Theme.GAP_S, Theme.GAP_XS + 2), Theme.BORDER, 1f, Theme.ARC_CARD);
        // TitledBorder breaks the line where the title sits, so the name reads "through" the border.
        section.setBorder(BorderFactory.createTitledBorder(outline, " " + title.toUpperCase() + " ",
                TitledBorder.LEADING, TitledBorder.TOP,
                Theme.font(Weight.MEDIUM, Theme.SIZE_CAPTION), Theme.TEXT_MUTED));
        return section;
    }

    /**
     * Stops a vertical layout stretching the box taller than its rows.
     * @param section the box, after its rows are added
     */
    public static void fitHeight(JPanel section) {
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));
    }
}
