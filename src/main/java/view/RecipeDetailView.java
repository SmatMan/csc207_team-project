package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import interface_adapter.recipe_detail.RecipeDetailController;
import interface_adapter.recipe_detail.RecipeDetailState;
import interface_adapter.recipe_detail.RecipeDetailViewModel;
import interface_adapter.recipe_detail.RecipeDetailViewModelListener;
import view.components.RecipeHtml;
import view.components.RoundedPhoto;
import view.components.ScrollablePanel;
import view.components.UiIcons;
import view.components.WrappingHtml;
import view.theme.Styles;
import view.theme.Theme;
import view.theme.Theme.Weight;

/**
 * The recipe screen: photo, name and description at the top, then the steps.
 * (The sidebar shows the recipe's ingredients meanwhile.)
 */
public class RecipeDetailView extends JPanel implements RecipeDetailViewModelListener {

    private static final int PHOTO_HEIGHT = 260;
    private static final int STEP_BADGE = 28;

    private final RecipeDetailViewModel recipeDetailViewModel;
    private RecipeDetailController recipeDetailController;

    private final JPanel content = new ScrollablePanel();
    private final JScrollPane scrollPane;

    public RecipeDetailView(RecipeDetailViewModel recipeDetailViewModel) {
        super(new BorderLayout(0, Theme.GAP_M));
        this.recipeDetailViewModel = recipeDetailViewModel;
        this.recipeDetailViewModel.addListener(this);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XL, Theme.GAP_XL + 4, 0, Theme.GAP_XL));

        final JButton back = Styles.clickable(Styles.secondaryButton(new JButton("Back to recipes")));
        // An icon rather than an arrow character, which the app's font doesn't have.
        back.setIcon(UiIcons.get(UiIcons.BACK, 16, Theme.TEXT));
        back.setIconTextGap(Theme.GAP_S);
        back.addActionListener(evt -> recipeDetailController.back());
        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(back);

        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP_XL, Theme.GAP_S));
        scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setRecipeDetailController(RecipeDetailController recipeDetailController) {
        this.recipeDetailController = recipeDetailController;
    }

    @Override
    public void stateChanged() {
        final RecipeDetailState state = recipeDetailViewModel.getState();
        content.removeAll();
        if (state.getError() != null) {
            content.add(label(state.getError(), Theme.font(Weight.MEDIUM, Theme.SIZE_HEADING), Theme.TEXT_MUTED));
        }
        else {
            showRecipe(state);
        }
        content.revalidate();
        content.repaint();
        // A new recipe starts at the top.
        scrollPane.getVerticalScrollBar().setValue(0);
    }

    private void showRecipe(RecipeDetailState state) {
        final RoundedPhoto photo = new RoundedPhoto(state.getImageUrl(), new Dimension(0, PHOTO_HEIGHT),
                Theme.ARC_CARD, false);
        photo.setAlignmentX(Component.LEFT_ALIGNMENT);
        photo.setMaximumSize(new Dimension(Integer.MAX_VALUE, PHOTO_HEIGHT));
        content.add(photo);
        content.add(Box.createVerticalStrut(Theme.GAP_L));

        content.add(label(state.getTitle(), Theme.font(Weight.BOLD, Theme.SIZE_TITLE + 4), Theme.TEXT));
        content.add(Box.createVerticalStrut(Theme.GAP_S));
        content.add(facts(state));

        if (!state.getSummaryHtml().isBlank()) {
            content.add(Box.createVerticalStrut(Theme.GAP_L));
            final WrappingHtml summary = new WrappingHtml(RecipeHtml.clean(state.getSummaryHtml()),
                    Theme.font(Weight.REGULAR, Theme.SIZE_BODY + 1), Theme.TEXT);
            summary.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(summary);
        }

        content.add(Box.createVerticalStrut(Theme.GAP_XL));
        content.add(label("Instructions", Theme.font(Weight.SEMIBOLD, Theme.SIZE_HEADING + 2), Theme.TEXT));
        content.add(Box.createVerticalStrut(Theme.GAP_M));
        if (state.getSteps().isEmpty()) {
            content.add(label("This recipe doesn't come with step-by-step instructions.",
                    Theme.font(Weight.REGULAR, Theme.SIZE_BODY), Theme.TEXT_MUTED));
        }
        for (int i = 0; i < state.getSteps().size(); i++) {
            content.add(step(i + 1, state.getSteps().get(i)));
            content.add(Box.createVerticalStrut(Theme.GAP_M));
        }

        if (!state.getSourceUrl().isBlank()) {
            content.add(Box.createVerticalStrut(Theme.GAP_S));
            content.add(sourceLink(state.getSourceName(), state.getSourceUrl()));
        }
    }

    /**
     * "★ 88   🌱 30   ⏱ Ready in 45 min   Prep 10 min   🍽 Serves 4", showing only what's known.
     */
    private static JPanel facts(RecipeDetailState state) {
        final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        final Font font = Theme.font(Weight.MEDIUM, Theme.SIZE_BODY);
        addFact(row, UiIcons.STAR, Theme.HIGHLIGHT_DEEP, state.getScoreText(), "Spoonacular score (out of 100)", font);
        addFact(row, UiIcons.HEALTH, Theme.SAGE, state.getHealthText(), "Health score (out of 100)", font);
        addFact(row, UiIcons.TIMER, Theme.TEXT_MUTED, state.getReadyText(), null, font);
        addFact(row, null, null, state.getPrepText(), null, font);
        addFact(row, UiIcons.SERVINGS, Theme.TEXT_MUTED, state.getServingsText(), null, font);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static void addFact(JPanel row, String icon, Color iconColor, String text, String tooltip,
                                Font font) {
        if (text == null) {
            return;
        }
        final JLabel fact = new JLabel(text);
        if (icon != null) {
            fact.setIcon(UiIcons.get(icon, 16, iconColor));
            fact.setIconTextGap(Theme.GAP_XS);
        }
        fact.setFont(font);
        fact.setForeground(Theme.TEXT);
        fact.setToolTipText(tooltip);
        fact.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.GAP_L));
        row.add(fact);
    }

    /**
     * A numbered step: the number in a tinted circle, then the text, wrapping to the width.
     */
    private static JPanel step(int number, String text) {
        final JPanel row = new JPanel(new BorderLayout(Theme.GAP_M, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JPanel badgeHolder = new JPanel(new BorderLayout());
        badgeHolder.setOpaque(false);
        badgeHolder.add(new StepBadge(number), BorderLayout.NORTH);
        row.add(badgeHolder, BorderLayout.WEST);
        row.add(new WrappingHtml(RecipeHtml.escape(text), Theme.font(Weight.REGULAR, Theme.SIZE_BODY + 1), Theme.TEXT),
                BorderLayout.CENTER);
        return row;
    }

    private static JComponent sourceLink(String name, String url) {
        final JButton link = Styles.clickable(new JButton("View the full recipe at " + (name.isBlank() ? "the source" : name)));
        link.putClientProperty("JButton.buttonType", "toolBarButton");
        link.setFont(Theme.font(Weight.MEDIUM, Theme.SIZE_BODY));
        link.setForeground(Theme.PRIMARY_HOVER);
        link.setAlignmentX(Component.LEFT_ALIGNMENT);
        link.addActionListener(evt -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                }
                catch (IOException | URISyntaxException ex) {
                    // Couldn't open the browser; nothing else to do.
                }
            }
        });
        return link;
    }

    private static JLabel label(String text, Font font, Color color) {
        final JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * A step number in a soft terracotta circle.
     */
    private static final class StepBadge extends JComponent {

        private final String number;

        private StepBadge(int number) {
            this.number = String.valueOf(number);
            setPreferredSize(new Dimension(STEP_BADGE, STEP_BADGE));
            setFont(Theme.font(Weight.SEMIBOLD, Theme.SIZE_BODY));
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY_TINT);
            g2.fillOval(0, 0, STEP_BADGE, STEP_BADGE);
            g2.setColor(Theme.PRIMARY_HOVER);
            g2.setFont(getFont());
            final int textWidth = g2.getFontMetrics().stringWidth(number);
            final int textY = (STEP_BADGE + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
            g2.drawString(number, (STEP_BADGE - textWidth) / 2, textY);
            g2.dispose();
        }
    }
}
