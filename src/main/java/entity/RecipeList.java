package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A list used to store recipes in a profile
*/
public class RecipeList {
    private String name;
    private final List<String> recipeIds = new ArrayList<>();

    public RecipeList(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("RecipeList name cannot be blank.");
        }
        this.name = name;
    }

    /**
     * Returns true if RecipeList is empty and false otherwise.
     */
    public boolean isEmpty(){
        return recipeIds.isEmpty();
    }

    /**
     * Returns the name of the RecipeList
     */
    public String getName(){
        return name;
    }

    /**
     * Returns a list of recipeIds
     */
    public List<String> getRecipeIds(){
        return new ArrayList<>(recipeIds);
    }

    /**
     * Adds a recipeId to RecipeList only if recipeId isn't already in RecipeList
     */
    public void addRecipeId(String recipeId){
        if(recipeId == null || recipeId.isBlank())
        {
            return;
        }
        if(!recipeIds.contains(recipeId)) {
            recipeIds.add(recipeId);
        }
    }

    /**
     * Remove and return a recipeId from RecipeList
     */
    public String removeRecipeId(String recipeId) {
            int index = recipeIds.indexOf(recipeId);
            if (index == -1) {
                return null;
            }
            return recipeIds.remove(index);
    }

    /**
     * Return recipeId if it exists in RecipeList.
     */
    public String getRecipeId(String recipeId) {
        int index = recipeIds.indexOf(recipeId);
        if (index == -1) {
            return null;
        }
        return recipeIds.get(index);
    }

    /**
     * Returns a list of recipeIds that either equal or contain search, not case-sensitive.
     */
    public List<String> searchRecipeIds(String search){
        String lowerSearch = search.toLowerCase();
        List<String> searchResults = new ArrayList<>();
        for(String recipeId:recipeIds){
            String lowerId = recipeId.toLowerCase();
            if(lowerId.contains(lowerSearch)){
                searchResults.add(recipeId);
            }
        }
        if(!searchResults.isEmpty()){
            return searchResults;
        }
        return new ArrayList<>();
    }

    /**
     * Add all recipes in recipeList into this RecipeList.
     */
    public void mergeRecipeList(RecipeList recipeList){
        if (recipeList == null) {
            return;
        }
        for(String recipeId:recipeList.getRecipeIds()){
            this.addRecipeId(recipeId);
        }
    }
}