package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Ingredient;
import entity.Recipe;
import use_case.find_recipes.FindRecipesDataAccessInterface;
import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;
import use_case.find_recipes.RecipeFilter;
import use_case.find_recipes.RecipeSearchResult;
import use_case.view_recipe.ViewRecipeDataAccessInterface;

/**
 * Recipe DAO backed by saved Spoonacular data (src/test/resources/mock/recipes.json), for
 * demos without using API quota. Returns the same recipes whatever is in the pantry (narrowed
 * by dish type and cuisine), pages through them, and waits briefly per page to feel like a
 * network call. Includes descriptions, steps and ingredient quantities for opened recipes.
 */
public class MockRecipeDataAccessObject implements FindRecipesDataAccessInterface,
                                                   ViewRecipeDataAccessInterface {

    private final List<Recipe> recipes = new ArrayList<>();
    private final Map<Integer, RecipeDetails> details = new HashMap<>();
    private final Map<Integer, List<String>> dishTypes = new HashMap<>();
    private final Map<Integer, List<String>> cuisines = new HashMap<>();
    private final int delayMillis;

    /**
     * Loads the mock recipes.
     * @param resource classpath path of the mock data, e.g. "/mock/recipes.json"
     * @param delayMillis how long each page takes to "load"
     */
    public MockRecipeDataAccessObject(String resource, int delayMillis) {
        this.delayMillis = delayMillis;
        final JSONArray results = readResource(resource).getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            final JSONObject result = results.getJSONObject(i);
            final List<IngredientLine> usedLines = lines(result.getJSONArray("usedIngredients"));
            final List<IngredientLine> missedLines = lines(result.getJSONArray("missedIngredients"));
            final List<Ingredient> used = ingredients(usedLines);
            final List<Ingredient> missed = ingredients(missedLines);
            final List<Ingredient> all = new ArrayList<>(used);
            all.addAll(missed);
            final int id = result.getInt("id");
            recipes.add(new Recipe(id, result.getString("title"), result.optString("image", ""), all, missed, used));

            details.put(id, RecipeDetails.builder()
                    .score(optDouble(result, "spoonacularScore"))
                    .healthScore(optDouble(result, "healthScore"))
                    .likes(optInt(result, "aggregateLikes"))
                    .readyInMinutes(optInt(result, "readyInMinutes"))
                    .preparationMinutes(optInt(result, "preparationMinutes"))
                    .servings(optInt(result, "servings"))
                    .diets(strings(result.optJSONArray("diets", new JSONArray())))
                    .summaryHtml(result.optString("summary", ""))
                    .steps(strings(result.optJSONArray("steps", new JSONArray())))
                    .usedLines(usedLines)
                    .missedLines(missedLines)
                    .source(result.optString("sourceName", ""), result.optString("sourceUrl", ""))
                    .build());
            dishTypes.put(id, strings(result.optJSONArray("dishTypes", new JSONArray())));
            cuisines.put(id, strings(result.optJSONArray("cuisines", new JSONArray())));
        }
    }

    @Override
    public Recipe getRecipe(int recipeId) {
        for (Recipe recipe : recipes) {
            if (recipe.getId() == recipeId) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public RecipeDetails getDetails(int recipeId) {
        return details.get(recipeId);
    }

    private static Double optDouble(JSONObject json, String key) {
        return json.isNull(key) || !json.has(key) ? null : json.getDouble(key);
    }

    private static Integer optInt(JSONObject json, String key) {
        return json.isNull(key) || !json.has(key) ? null : json.getInt(key);
    }

    private static List<IngredientLine> lines(JSONArray array) {
        final List<IngredientLine> lines = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject line = array.getJSONObject(i);
            if (!line.optString("name", "").isBlank()) {
                lines.add(new IngredientLine(line.getString("name"), optDouble(line, "amount"),
                        line.optString("unit", ""), line.optString("original", "")));
            }
        }
        return lines;
    }

    private static List<Ingredient> ingredients(List<IngredientLine> lines) {
        final List<Ingredient> ingredients = new ArrayList<>();
        for (IngredientLine line : lines) {
            ingredients.add(new Ingredient(line.getName()));
        }
        return ingredients;
    }

    @Override
    public RecipeSearchResult findRecipes(List<Ingredient> ingredients, RecipeFilter filter, int offset, int count) {
        try {
            Thread.sleep(delayMillis);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        // Like Spoonacular's "type" and "cuisine" parameters: keep recipes tagged with them.
        final List<Recipe> matching = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (tagged(dishTypes.get(recipe.getId()), filter.getDishType())
                    && tagged(cuisines.get(recipe.getId()), filter.getCuisine())) {
                matching.add(recipe);
            }
        }
        final int from = Math.min(offset, matching.size());
        final int to = Math.min(offset + count, matching.size());
        return new RecipeSearchResult(matching.subList(from, to), matching.size(), details);
    }

    private static boolean tagged(List<String> tags, String wanted) {
        if (wanted == null) {
            return true;
        }
        for (String tag : tags) {
            if (tag.equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> strings(JSONArray array) {
        final List<String> strings = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            strings.add(array.getString(i));
        }
        return strings;
    }

    private static JSONObject readResource(String resource) {
        try (InputStream in = MockRecipeDataAccessObject.class.getResourceAsStream(resource)) {
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
