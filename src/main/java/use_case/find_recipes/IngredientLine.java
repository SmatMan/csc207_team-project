package use_case.find_recipes;

/**
 * One ingredient as a recipe lists it: the name, how much, and the recipe's own wording
 * (e.g. "1/2 red onion, sliced thinly").
 */
public class IngredientLine {

    private final String name;
    private final Double amount;
    private final String unit;
    private final String original;

    /**
     * Creates an ingredient line.
     * @param name the ingredient, e.g. "onion"
     * @param amount how much, e.g. 0.5, or null if unknown
     * @param unit the unit, e.g. "cups" ("" for a count, like 2 eggs)
     * @param original the recipe's own line for it, or "" if unknown
     */
    public IngredientLine(String name, Double amount, String unit, String original) {
        this.name = name;
        this.amount = amount;
        this.unit = unit == null ? "" : unit;
        this.original = original == null ? "" : original;
    }

    public String getName() {
        return name;
    }

    public Double getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    public String getOriginal() {
        return original;
    }
}
