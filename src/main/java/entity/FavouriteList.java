package entity;

/**
 * A list used to store favourite recipes in a profile. Only one instance can exist and already exists upon
 * startup.
 */
public class FavouriteList extends RecipeList {

    public FavouriteList() {
        super("Favourites");
    }

    public FavouriteList(String name) {
        super(name);
    }


}
