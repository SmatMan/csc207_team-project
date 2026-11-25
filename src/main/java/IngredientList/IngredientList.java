package IngredientList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import Class.Ingredient;

public class IngredientList {
    private ArrayList<String> IngredientNames;

    public IngredientList()  {
        //Declare a list that is going to be used to store the ingredient names
        this.IngredientNames = new ArrayList<>();
    }

    public ArrayList<String> getIngredients() {
        return this.IngredientNames;
    }

    public void addToList(ArrayList<Ingredient> ingredients) {
        for (Ingredient item : ingredients) {
            this.IngredientNames.add(item.name);

        }
    }

    public void addToList(Ingredient ingredients) {
        this.IngredientNames.add(ingredients.name);
    }

    public void DeleteFromList(String name) {
        int index = this.IngredientNames.indexOf(name);
        IngredientNames.remove(index);
    }
}
