package interface_adapter.recipe_detail;

import java.util.ArrayList;
import java.util.List;

/**
 * The state for the Recipe Detail View Model: one recipe, ready to show in full.
 * Texts are null (or lists empty) when unknown.
 */
public class RecipeDetailState {
    private int id;
    private String title = "";
    private String imageUrl = "";
    private String summaryHtml = "";
    private List<String> steps = new ArrayList<>();
    private String scoreText;
    private String healthText;
    private String readyText;
    private String prepText;
    private String servingsText;
    private List<Line> have = new ArrayList<>();
    private List<Line> need = new ArrayList<>();
    private String sourceName = "";
    private String sourceUrl = "";
    private String error;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Returns the description as the recipe service wrote it: HTML, with bold facts and links.
     * @return the description, or ""
     */
    public String getSummaryHtml() {
        return summaryHtml;
    }

    public List<String> getSteps() {
        return steps;
    }

    public String getScoreText() {
        return scoreText;
    }

    public String getHealthText() {
        return healthText;
    }

    public String getReadyText() {
        return readyText;
    }

    public String getPrepText() {
        return prepText;
    }

    public String getServingsText() {
        return servingsText;
    }

    public List<Line> getHave() {
        return have;
    }

    public List<Line> getNeed() {
        return need;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getError() {
        return error;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSummaryHtml(String summaryHtml) {
        this.summaryHtml = summaryHtml;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
    }

    public void setHealthText(String healthText) {
        this.healthText = healthText;
    }

    public void setReadyText(String readyText) {
        this.readyText = readyText;
    }

    public void setPrepText(String prepText) {
        this.prepText = prepText;
    }

    public void setServingsText(String servingsText) {
        this.servingsText = servingsText;
    }

    public void setHave(List<Line> have) {
        this.have = have;
    }

    public void setNeed(List<Line> need) {
        this.need = need;
    }

    public void setSource(String name, String url) {
        this.sourceName = name;
        this.sourceUrl = url;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * One ingredient row: its name, a readable quantity (e.g. "1 1/2 cups"), and the
     * recipe's own wording for it.
     */
    public static class Line {
        private final String name;
        private final String quantity;
        private final String original;

        public Line(String name, String quantity, String original) {
            this.name = name;
            this.quantity = quantity;
            this.original = original;
        }

        public String getName() {
            return name;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getOriginal() {
            return original;
        }
    }
}
