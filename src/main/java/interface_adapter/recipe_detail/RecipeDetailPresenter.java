package interface_adapter.recipe_detail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import interface_adapter.ViewManagerModel;
import use_case.find_recipes.IngredientLine;
import use_case.find_recipes.RecipeDetails;
import use_case.view_recipe.ViewRecipeOutputBoundary;
import use_case.view_recipe.ViewRecipeOutputData;

/**
 * The Presenter for the View Recipe Use Case: fills the recipe screen and switches to it.
 */
public class RecipeDetailPresenter implements ViewRecipeOutputBoundary {

    private final ViewManagerModel viewManagerModel;
    private final RecipeDetailViewModel recipeDetailViewModel;

    public RecipeDetailPresenter(ViewManagerModel viewManagerModel, RecipeDetailViewModel recipeDetailViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.recipeDetailViewModel = recipeDetailViewModel;
    }

    @Override
    public void prepareSuccessView(ViewRecipeOutputData response) {
        final RecipeDetails details = response.getDetails();
        final RecipeDetailState state = new RecipeDetailState();
        state.setId(response.getId());
        state.setTitle(response.getTitle());
        state.setImageUrl(response.getImageUrl());
        state.setSummaryHtml(details.getSummaryHtml());
        state.setSteps(new ArrayList<>(details.getSteps()));
        state.setScoreText(outOf100(details.getScore()));
        state.setHealthText(outOf100(details.getHealthScore()));
        state.setReadyText(minutes("Ready in ", details.getReadyInMinutes()));
        state.setPrepText(minutes("Prep ", details.getPreparationMinutes()));
        state.setServingsText(details.getServings() == null ? null : "Serves " + details.getServings());
        state.setHave(lines(response.getHave()));
        state.setNeed(lines(response.getNeed()));
        state.setSource(details.getSourceName(), details.getSourceUrl());

        recipeDetailViewModel.setState(state);
        recipeDetailViewModel.notifyStateChanged();

        viewManagerModel.setState(RecipeDetailViewModel.VIEW_NAME);
        viewManagerModel.notifyViewChanged();
    }

    @Override
    public void prepareFailView(String error) {
        final RecipeDetailState state = new RecipeDetailState();
        state.setError(error);
        recipeDetailViewModel.setState(state);
        recipeDetailViewModel.notifyStateChanged();

        viewManagerModel.setState(RecipeDetailViewModel.VIEW_NAME);
        viewManagerModel.notifyViewChanged();
    }

    private static List<RecipeDetailState.Line> lines(List<IngredientLine> ingredients) {
        final List<RecipeDetailState.Line> lines = new ArrayList<>();
        for (IngredientLine ingredient : ingredients) {
            lines.add(new RecipeDetailState.Line(capitalise(ingredient.getName()),
                    quantity(ingredient.getAmount(), ingredient.getUnit()), ingredient.getOriginal()));
        }
        return lines;
    }

    /**
     * A readable quantity, using kitchen fractions: 0.5 and "cups" to "1/2 cups", 1.5 to "1 1/2",
     * 2 to "2". Empty if the amount is unknown.
     */
    static String quantity(Double amount, String unit) {
        if (amount == null || amount <= 0) {
            return "";
        }
        final String number = kitchenNumber(amount);
        return unit == null || unit.isBlank() ? number : number + " " + unit;
    }

    private static String kitchenNumber(double amount) {
        final long whole = (long) Math.floor(amount);
        final double part = amount - whole;
        final String[][] fractions = {
            {"0.25", "1/4"}, {"0.333", "1/3"}, {"0.5", "1/2"}, {"0.667", "2/3"}, {"0.75", "3/4"},
        };
        if (part < 0.05) {
            return String.valueOf(whole);
        }
        if (part > 0.95) {
            return String.valueOf(whole + 1);
        }
        for (String[] fraction : fractions) {
            if (Math.abs(part - Double.parseDouble(fraction[0])) < 0.04) {
                return whole == 0 ? fraction[1] : whole + " " + fraction[1];
            }
        }
        // Not a common fraction: one decimal place is plenty in a kitchen.
        final String decimal = String.format(Locale.ROOT, "%.1f", amount);
        return decimal.endsWith(".0") ? decimal.substring(0, decimal.length() - 2) : decimal;
    }

    private static String minutes(String prefix, Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return null;
        }
        if (minutes < 60) {
            return prefix + minutes + " min";
        }
        final int rest = minutes % 60;
        return prefix + (minutes / 60) + " h" + (rest == 0 ? "" : " " + rest + " min");
    }

    private static String outOf100(Double score) {
        return score == null ? null : String.valueOf(Math.round(Math.max(0, Math.min(100, score))));
    }

    private static String capitalise(String text) {
        return text == null || text.isEmpty() ? "" : text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
