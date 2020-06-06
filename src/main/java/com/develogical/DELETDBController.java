package com.develogical;

import java.util.List;

public interface DELETDBController {
    List<DELETIngredient> lookupMealIngredients(String meal);
    double lookupIngredientNutrition(DELETIngredient ingredient);
}
