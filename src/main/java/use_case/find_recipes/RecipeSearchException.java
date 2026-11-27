package use_case.find_recipes;

/**
 * Thrown when recipes can't be fetched, e.g. the recipe service is unreachable
 * or its daily limit has been reached.
 */
public class RecipeSearchException extends Exception {

    public RecipeSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
