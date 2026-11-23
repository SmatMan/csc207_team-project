//This file is for the API call to our Spoonocular APi that will fetch recipes based on the ingredients that we have
package API;

//import Ingredient type class into this file

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import entity.Ingredient;
import entity.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;


public class ApiCall {

    public static ArrayList<Recipe> MakeAPICall(List<Ingredient> Ingredients) {
        //Create a string to pass into the API call
        String ingredientstring = "";
        for (Ingredient item : Ingredients) {
            ingredientstring += item.getName() + ",";
        }
        //format the string so that it can be passed into the URL
        String encoded = URLEncoder.encode(ingredientstring, StandardCharsets.UTF_8);

        String url = "https://api.spoonacular.com/recipes/findByIngredients"
                + "?ingredients=" + encoded + "&number=5&apiKey=0a7b9f34c331474f9b21c68ac8692cb5";

        //Call the APi and then pass into into the parser
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ParseAPICall(response);

        }  catch (IOException | InterruptedException e) {
            throw new RuntimeException("API call failed", e);
        }


    }

    public static ArrayList<Recipe> ParseAPICall(HttpResponse<String> response) {
        //Catch the I/O error
        JSONArray recipes = new JSONArray(response.body());
        ArrayList<Recipe> RecipeArray = new ArrayList<>();

        //iterate through the list of recipes
        for (int i = 0; recipes.length() > i; i++) {
            JSONObject recipeJSON = recipes.getJSONObject(i);
            int id = recipeJSON.getInt("id");
            String title = recipeJSON.getString("title");
            String image = recipeJSON.getString("image");
            ArrayList<Ingredient> ingredientsArray = new ArrayList<>();
            ArrayList<Ingredient> missedIngredients = new ArrayList<>();

            //get the names and other characteristics of the missing ingredients
            JSONArray missedingredientsJSON = recipeJSON.getJSONArray("missedIngredients");
            for (int j = 0; j < missedingredientsJSON.length(); j++) {
                JSONObject missIngingrdient = missedingredientsJSON.getJSONObject(j);
                String missedIngredientname = missIngingrdient.getString("name");
                Ingredient missedIngredientObject = new Ingredient(missedIngredientname);
                missedIngredients.add(missedIngredientObject);
                ingredientsArray.add(missedIngredientObject);
            }

            //get the names of the used ingredients that we already have
            ArrayList<Ingredient> usedingredients = new ArrayList<>();
            JSONArray usedingredientsJSON = recipeJSON.getJSONArray("usedIngredients");
            for (int k = 0; k < usedingredientsJSON.length(); k++) {
                JSONObject usedingredient = usedingredientsJSON.getJSONObject(k);
                String usedIngredientName = usedingredient.getString("name");
                Ingredient usedIngredientObject = new Ingredient(usedIngredientName);
                usedingredients.add(usedIngredientObject);
                ingredientsArray.add(usedIngredientObject);
            }
            Recipe recipe = new Recipe(id, title, image, ingredientsArray, missedIngredients, usedingredients);
            RecipeArray.add((recipe));
        }

        return RecipeArray;
    }

}

