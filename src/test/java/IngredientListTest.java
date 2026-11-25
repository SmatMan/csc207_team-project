import Class.Ingredient;
import java.util.ArrayList;
import IngredientList.IngredientList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class IngredientListTest {

    @Test
    public void testAddToList() {
        IngredientList Ingredients1 = new IngredientList();
        IngredientList Ingredients2 = new IngredientList();
        Ingredient Apple = new Ingredient("Apple");
        Ingredient Banana = new Ingredient("Banana");

        ArrayList<Ingredient> TempIngredientList = new ArrayList<>();
        TempIngredientList.add(Apple);
        TempIngredientList.add(Banana);

        Ingredients1.addToList(Apple);
        Ingredients2.addToList(TempIngredientList);

        ArrayList<String> ExpectedIngredientList1 = new ArrayList<>();
        ExpectedIngredientList1.add("Apple");

        ArrayList<String> ExpectedIngredientList2 = new ArrayList<>();
        ExpectedIngredientList2.add("Apple");
        ExpectedIngredientList2.add("Banana");

        assertEquals(Ingredients2.getIngredients(), ExpectedIngredientList2);
        assertEquals(Ingredients1.getIngredients(), ExpectedIngredientList1);

    }
}

