package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Ingredient;
import entity.Intolerance;
import use_case.create_profile.IntoleranceCatalogue;

/**
 * Reads the catalogue of intolerance categories from a JSON file bundled with the app.
 *
 * <p>NOTE: the bundled catalogue (restrictions.json) is a placeholder. Its categories and
 * ingredients will be expanded or trimmed to match the ingredient list produced by the
 * Spoonacular API calls (use case 2).</p>
 */
public class FileIntoleranceCatalogue implements IntoleranceCatalogue {

    private final List<Intolerance> intolerances = new ArrayList<>();

    /**
     * Reads the whole catalogue once, at startup. After that it only hands back the list.
     * @param catalogueResource classpath path of the catalogue, e.g. "/restrictions.json"
     * @throws RuntimeException if the resource is missing or invalid
     */
    public FileIntoleranceCatalogue(String catalogueResource) {
        final JSONObject catalogue = readResource(catalogueResource);
        // Only the "intolerances" array matters. The file also has a "presets" array
        // from the old diet-preset feature, and we skip it.
        final JSONArray intoleranceArray = catalogue.getJSONArray("intolerances");
        for (int i = 0; i < intoleranceArray.length(); i++) {
            final JSONObject intolerance = intoleranceArray.getJSONObject(i);
            final JSONArray ingredientArray = intolerance.getJSONArray("ingredients");
            final List<Ingredient> ingredients = new ArrayList<>();
            for (int j = 0; j < ingredientArray.length(); j++) {
                ingredients.add(new Ingredient(ingredientArray.getString(j)));
            }
            intolerances.add(new Intolerance(intolerance.getString("name"), ingredients));
        }
    }

    @Override
    public List<Intolerance> getAvailableIntolerances() {
        return intolerances;
    }

    // Loads a file from the classpath and parses it. Any failure throws a RuntimeException.
    // That fits here. A missing or broken catalogue is a setup mistake, and no user input can cause it.
    private static JSONObject readResource(String resource) {
        try (InputStream in = FileIntoleranceCatalogue.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new RuntimeException("Missing resource: " + resource);
            }
            return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
