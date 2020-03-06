package com.develogical;

import java.util.List;

public class QueryProcessor {
    private final DBController dbController;

    public QueryProcessor(DBController dbController) {
        this.dbController = dbController;
    }

    public String getNutritionalData(String meal) {
        long startTime = System.currentTimeMillis();
        List<Ingredient> mealIngredients = dbController.lookupMealIngredients(meal);
        long endTime = System.currentTimeMillis();
        System.out.println("EXECTIME(ms) for lookupMealIngredients() = " + (endTime - startTime));
        System.out.println("nb of ingredients in meal: " + mealIngredients.size());

        double kcals = 0.0;
        for(Ingredient ingredient : mealIngredients) {
            startTime = System.currentTimeMillis();
            kcals += dbController.lookupIngredientNutrition(ingredient);
            endTime = System.currentTimeMillis();
            System.out.println("EXECTIME(ms) for lookupIngredientNutrition() = " + (endTime - startTime));
        }
        System.out.println("nb of kcals in meal: "  + kcals);
        return "db lookup result: " + kcals;
    }
}
