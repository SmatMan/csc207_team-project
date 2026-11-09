//This file is for the API call to our Spoonocular APi that will fetch recipes based on the ingredients that we have


//import Ingredient type class into this file

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class Ingredient {
    String name;
}

public class ApiCall {

    void MakeIngredientList(Ingredient[] ingredients)  {
        //Declare a list that is going to be used to store the ingredient names
        List<String> IngredientNames = new ArrayList<>();
        String IngreidentString = "";

        for (Ingredient item : ingredients) {
            IngredientNames.add(item.name);

        }
    }

    void MakeAPICall(List<Ingredient> IngredientNames) {
        String url = "https://api.spoonacular.com/recipes/findByIngredients"
                + "?ingredients=flour,sugar,eggs&number=5&apiKey=YOUR_KEY";

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

