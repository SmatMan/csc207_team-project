package entity;

import java.util.Locale;
import java.util.List;

/**
 * A recipe, which has anid, name
 * image, an ingredients list, and a missed ingredients list
 */
public class Recipe {

    private final int id;
    private final String name;
    private final String imageURL;
    private final List<Ingredient> ingredients;
    private final List<Ingredient> missingIngredients;
    private final List<Ingredient> usedIngredients;

    /**
     * Creates an recipe with the given non-empty name, usedingredients,
     * missing ingredients, and total ingredients
     * @throws IllegalArgumentException if the name and the ingredients list are null or blank
     * if no iamge URL is provided it reverts to a default
     */

    public Recipe(int id, String name, String imageURL, List<Ingredient> ingredients,
                  List<Ingredient> missingIngredients, List<Ingredient> usedIngredients) {
        if (name == null || name.isBlank() || id <= 0 || ingredients == null) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }

        if (imageURL == null) {
            this.imageURL = "";
        } else {
            this.imageURL = imageURL;
        }

        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.missingIngredients = missingIngredients;
        this.usedIngredients = usedIngredients;
    }

    public int getId() {return id; }
    public String getName() {return name; }
    public String getImage() {return imageURL;}
    public List<Ingredient> getIngredients() {return ingredients;}
    public List<Ingredient> getMissingIngredients() {return missingIngredients;}
    public List<Ingredient> getUsedIngredients() {return usedIngredients;}

}

