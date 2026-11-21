//This file is for the API call to our Spoonocular APi that will fetch recipes based on the ingredients that we have
package API;

//import Ingredient type class into this file

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import Class.Ingredient;


public class ApiCall {

    public static void MakeAPICall(List<Ingredient> Ingredients) {
        //Create a string to pass into the API call
        String ingredientstring = "";
        for (Ingredient item : Ingredients) {
            ingredientstring += item.name + ",";
        }
        //format the string so that it can be passed into the URL
        String encoded = URLEncoder.encode(ingredientstring, StandardCharsets.UTF_8);

        String url = "https://api.spoonacular.com/recipes/findByIngredients"
                + "?ingredients=" + encoded + "&number=5&apiKey=0a7b9f34c331474f9b21c68ac8692cb5";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .GET()
                .build();

        //Catch the I/O error
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println("ur dumb");
        }
    }


}

