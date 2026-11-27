package use_case.find_recipes;

/**
 * Narrows a recipe search to a dish type (e.g. "main course") and/or a cuisine
 * (e.g. "Italian"). Either can be null, meaning any.
 */
public class RecipeFilter {

    /** No filter: any dish type, any cuisine. */
    public static final RecipeFilter NONE = new RecipeFilter(null, null);

    private final String dishType;
    private final String cuisine;

    /**
     * Creates a filter.
     * @param dishType the dish type, or null for any
     * @param cuisine the cuisine, or null for any
     */
    public RecipeFilter(String dishType, String cuisine) {
        this.dishType = blankToNull(dishType);
        this.cuisine = blankToNull(cuisine);
    }

    public String getDishType() {
        return dishType;
    }

    public String getCuisine() {
        return cuisine;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
