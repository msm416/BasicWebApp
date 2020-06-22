package com.develogical;

import java.util.List;

public class QueryProcessor {
    private final DBController dbController;

    public QueryProcessor(DBController dbController) {
        this.dbController = dbController;
    }

    public String getNutritionalData(String meal) {
        List<Ingredient> mealIngredients = dbController.lookupMealIngredients(meal);

//        double kcals = 0.0;
//        for(Ingredient ingredient : mealIngredients) {
//            kcals += dbController.lookupIngredientNutrition(ingredient);
//        }

        double kcals = mealIngredients.parallelStream()
                .map(dbController::lookupIngredientNutrition)
                .reduce(0d, (a,b) -> a + b);

        return "nb of kcals in meal: "  + kcals;
    }

    public String suggestAMeal(int dishComplexity) {
        assert (1 <= dishComplexity && dishComplexity <= 5);
        long startTime = System.currentTimeMillis();
        String suggestedMeal = dbController.lookupTopMealByComplexity(dishComplexity);
        long endTime = System.currentTimeMillis();
        //System.out.println("EXECTIME(ms) for lookupTopMealByComplexity() = " + (endTime - startTime));


        long startTimeMixture = System.currentTimeMillis();
        int remNbOfApiCalls = 5;
        for(String ingredient : suggestedMeal.split(":")[1].split(",")) {
            if(remNbOfApiCalls == 0) {
                break;
            }
            startTime = System.currentTimeMillis();
            dbController.lookupOnApiIngredientDetails(ingredient);
            endTime = System.currentTimeMillis();
            //System.out.println("EXECTIME(ms) for lookupOnApiIngredientDetails() = " + (endTime - startTime));

            remNbOfApiCalls --;
        }
        long endTimeMixture = System.currentTimeMillis();
        //System.out.println("EXECTIME(ms) for combinedlookupOnApiIngredientDetails() = " + (endTimeMixture - startTimeMixture));
        return suggestedMeal.split(":")[0];
    }
}
