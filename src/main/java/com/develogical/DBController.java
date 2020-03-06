package com.develogical;

import java.util.List;

public interface DBController {
    List<Ingredient> lookupMealIngredients(String meal);
    double lookupIngredientNutrition(Ingredient ingredient);
}
