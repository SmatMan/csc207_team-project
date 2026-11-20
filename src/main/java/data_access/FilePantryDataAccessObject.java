package data_access;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Ingredient;
import entity.Pantry;
import use_case.add_ingredient.AddIngredientDataAccessInterface;
import use_case.load_pantry.LoadPantryDataAccessInterface;
import use_case.remove_ingredient.RemoveIngredientDataAccessInterface;

/**
 * DAO for the user's pantry, saved to a JSON file as a list of ingredient names.
 */
public class FilePantryDataAccessObject implements LoadPantryDataAccessInterface,
                                                   AddIngredientDataAccessInterface,
                                                   RemoveIngredientDataAccessInterface {

    private static final String INGREDIENTS = "ingredients";

    private final File pantryFile;
    private Pantry pantry;

    /**
     * Construct this DAO from the user's saved pantry, if any.
     * @param pantryPath path of the file the user's pantry is saved to
     * @throws RuntimeException if the saved pantry can't be read
     */
    public FilePantryDataAccessObject(String pantryPath) {
        pantryFile = new File(pantryPath);
        if (pantryFile.length() == 0) {
            pantry = new Pantry();
        }
        else {
            pantry = readPantry();
        }
    }

    @Override
    public Pantry getPantry() {
        return pantry;
    }

    @Override
    public void save(Pantry userPantry) {
        this.pantry = userPantry;

        final JSONArray names = new JSONArray();
        for (Ingredient ingredient : userPantry.getIngredients()) {
            names.put(ingredient.getName());
        }
        final JSONObject json = new JSONObject();
        json.put(INGREDIENTS, names);
        try {
            Files.writeString(pantryFile.toPath(), json.toString(2), StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Pantry readPantry() {
        final JSONObject json;
        try {
            json = new JSONObject(Files.readString(pantryFile.toPath(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Skip blank names, e.g. after editing pantry.json by hand.
        final List<Ingredient> ingredients = new ArrayList<>();
        for (Object name : json.optJSONArray(INGREDIENTS, new JSONArray())) {
            if (!name.toString().isBlank()) {
                ingredients.add(new Ingredient(name.toString()));
            }
        }
        return new Pantry(ingredients);
    }
}
