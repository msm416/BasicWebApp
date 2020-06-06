package com.develogical;

import java.util.List;

public class DELETQueryProcessor {
    private final DELETDBController dbController;

    public DELETQueryProcessor(DELETDBController dbController) {
        this.dbController = dbController;
    }


    public String getNutritionalData(String meal) {
        List<DELETIngredient> ingredients = dbController.lookupMealIngredients(meal);
        for(int i = 0; i < ingredients.size(); i++) {
            dbController.lookupIngredientNutrition(ingredients.get(i));
        }
        return "";
    }
}
