package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Ingredient;
import entity.KnownIngredient;
import use_case.search_ingredients.SearchIngredientsDataAccessInterface;

/**
 * DAO for the ingredient corpus: every ingredient the app knows about, read from a
 * JSON resource bundled with the app. The corpus is read-only.
 *
 * <p>NOTE: the bundled corpus (ingredients.json) is a placeholder. It will be replaced or
 * expanded with the ingredient list produced by the Spoonacular API calls (use case 2).</p>
 */
public class FileIngredientCorpusDataAccessObject implements SearchIngredientsDataAccessInterface {

    private final List<KnownIngredient> knownIngredients = new ArrayList<>();

    /**
     * Construct this DAO from the bundled corpus.
     * @param corpusResource classpath path of the corpus, e.g. "/ingredients.json"
     * @throws RuntimeException if the corpus is missing, invalid, or names an ingredient twice
     */
    public FileIngredientCorpusDataAccessObject(String corpusResource) {
        final JSONArray entries = readResource(corpusResource).getJSONArray("ingredients");
        final Set<Ingredient> seen = new HashSet<>();
        for (int i = 0; i < entries.length(); i++) {
            final JSONObject entry = entries.getJSONObject(i);
            final Ingredient ingredient = new Ingredient(entry.getString("name"));
            if (!seen.add(ingredient)) {
                throw new RuntimeException("Ingredient \"" + ingredient + "\" appears twice in " + corpusResource);
            }

            final List<String> aliases = new ArrayList<>();
            final JSONArray aliasArray = entry.optJSONArray("aliases", new JSONArray());
            for (int j = 0; j < aliasArray.length(); j++) {
                aliases.add(aliasArray.getString(j));
            }
            knownIngredients.add(new KnownIngredient(ingredient, entry.getString("category"), aliases));
        }
    }

    @Override
    public List<KnownIngredient> getKnownIngredients() {
        return Collections.unmodifiableList(knownIngredients);
    }

    private static JSONObject readResource(String resource) {
        try (InputStream in = FileIngredientCorpusDataAccessObject.class.getResourceAsStream(resource)) {
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
