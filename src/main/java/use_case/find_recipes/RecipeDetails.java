package use_case.find_recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extra information about a recipe that the recipe service may provide alongside it.
 * Any field can be null (or empty, for lists and text) when it isn't known.
 */
public class RecipeDetails {

    private final Double score;
    private final Double healthScore;
    private final Integer likes;
    private final Integer readyInMinutes;
    private final Integer preparationMinutes;
    private final Integer servings;
    private final List<String> diets;
    private final String summaryHtml;
    private final List<String> steps;
    private final List<IngredientLine> usedLines;
    private final List<IngredientLine> missedLines;
    private final String sourceName;
    private final String sourceUrl;

    /**
     * Creates recipe details with the main facts only.
     * @param score the recipe's quality score from 0 to 100, or null
     * @param healthScore how healthy the recipe is, from 0 to 100, or null
     * @param likes how many people liked the recipe, or null
     * @param readyInMinutes how long it takes to make, or null
     * @param servings how many people it serves, or null
     * @param diets diets the recipe suits, e.g. "gluten free"
     */
    public RecipeDetails(Double score, Double healthScore, Integer likes, Integer readyInMinutes,
                         Integer servings, List<String> diets) {
        this(builder().score(score).healthScore(healthScore).likes(likes)
                .readyInMinutes(readyInMinutes).servings(servings).diets(diets));
    }

    private RecipeDetails(Builder builder) {
        this.score = builder.score;
        this.healthScore = builder.healthScore;
        this.likes = builder.likes;
        this.readyInMinutes = builder.readyInMinutes;
        this.preparationMinutes = builder.preparationMinutes;
        this.servings = builder.servings;
        this.diets = Collections.unmodifiableList(new ArrayList<>(builder.diets));
        this.summaryHtml = builder.summaryHtml;
        this.steps = Collections.unmodifiableList(new ArrayList<>(builder.steps));
        this.usedLines = Collections.unmodifiableList(new ArrayList<>(builder.usedLines));
        this.missedLines = Collections.unmodifiableList(new ArrayList<>(builder.missedLines));
        this.sourceName = builder.sourceName;
        this.sourceUrl = builder.sourceUrl;
    }

    /**
     * Starts building recipe details; anything not set stays unknown.
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public Double getScore() {
        return score;
    }

    public Double getHealthScore() {
        return healthScore;
    }

    public Integer getLikes() {
        return likes;
    }

    public Integer getReadyInMinutes() {
        return readyInMinutes;
    }

    public Integer getPreparationMinutes() {
        return preparationMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public List<String> getDiets() {
        return diets;
    }

    /**
     * Returns the recipe's description as the service wrote it: HTML, with bold facts and links.
     * @return the description, or "" if there is none
     */
    public String getSummaryHtml() {
        return summaryHtml;
    }

    public List<String> getSteps() {
        return steps;
    }

    /**
     * Returns the ingredients the user has, with quantities.
     * @return the used ingredient lines (empty if unknown)
     */
    public List<IngredientLine> getUsedLines() {
        return usedLines;
    }

    /**
     * Returns the ingredients the user would need to buy, with quantities.
     * @return the missing ingredient lines (empty if unknown)
     */
    public List<IngredientLine> getMissedLines() {
        return missedLines;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Builds {@link RecipeDetails} one field at a time.
     */
    public static final class Builder {
        private Double score;
        private Double healthScore;
        private Integer likes;
        private Integer readyInMinutes;
        private Integer preparationMinutes;
        private Integer servings;
        private List<String> diets = new ArrayList<>();
        private String summaryHtml = "";
        private List<String> steps = new ArrayList<>();
        private List<IngredientLine> usedLines = new ArrayList<>();
        private List<IngredientLine> missedLines = new ArrayList<>();
        private String sourceName = "";
        private String sourceUrl = "";

        private Builder() {
        }

        public Builder score(Double value) {
            this.score = value;
            return this;
        }

        public Builder healthScore(Double value) {
            this.healthScore = value;
            return this;
        }

        public Builder likes(Integer value) {
            this.likes = value;
            return this;
        }

        public Builder readyInMinutes(Integer value) {
            this.readyInMinutes = value;
            return this;
        }

        public Builder preparationMinutes(Integer value) {
            this.preparationMinutes = value;
            return this;
        }

        public Builder servings(Integer value) {
            this.servings = value;
            return this;
        }

        public Builder diets(List<String> value) {
            this.diets = value == null ? new ArrayList<>() : value;
            return this;
        }

        public Builder summaryHtml(String value) {
            this.summaryHtml = value == null ? "" : value;
            return this;
        }

        public Builder steps(List<String> value) {
            this.steps = value == null ? new ArrayList<>() : value;
            return this;
        }

        public Builder usedLines(List<IngredientLine> value) {
            this.usedLines = value == null ? new ArrayList<>() : value;
            return this;
        }

        public Builder missedLines(List<IngredientLine> value) {
            this.missedLines = value == null ? new ArrayList<>() : value;
            return this;
        }

        public Builder source(String name, String url) {
            this.sourceName = name == null ? "" : name;
            this.sourceUrl = url == null ? "" : url;
            return this;
        }

        public RecipeDetails build() {
            return new RecipeDetails(this);
        }
    }
}
