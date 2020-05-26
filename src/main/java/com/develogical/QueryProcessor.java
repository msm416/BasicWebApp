package com.develogical;

import java.util.List;

public class QueryProcessor {
    private final DBController dbController;

    public QueryProcessor(DBController dbController) {
        this.dbController = dbController;
    }

    public String getNutritionalData(String meal) {
        //long startTime = System.currentTimeMillis();
        List<Ingredient> mealIngredients = dbController.lookupMealIngredients(meal);
        long startTime = System.currentTimeMillis();
        //long endTime = System.currentTimeMillis();
        //System.out.println("EXECTIME(ms) for lookupMealIngredients() = " + (endTime - startTime));
        //System.out.println("nb of ingredients in meal: " + mealIngredients.size());

        double kcals = 0.0;
        for(Ingredient ingredient : mealIngredients) {
            //startTime = System.currentTimeMillis();
            kcals += dbController.lookupIngredientNutrition(ingredient);
            //endTime = System.currentTimeMillis();
            //System.out.println("EXECTIME(ms) for lookupIngredientNutrition() = " + (endTime - startTime));
        }

        long endTime = System.currentTimeMillis();
        System.out.println("EXECTIME(ms) for lookupIngredientNutritionCombinedSequential() = " + (endTime - startTime));

//        double kcals_par = mealIngredients.parallelStream()
//                .map(dbController::lookupIngredientNutrition)
//                .reduce(0d, (a,b) -> a + b);
//        long endTime = System.currentTimeMillis();
//        System.out.println("EXECTIME(ms) for lookupIngredientNutritionCombinedParallel() = " + (endTime - startTime));

        //System.out.println("nb of kcals in meal: "  + kcals);
        return "nb of kcals in meal: "  + kcals;
    }

    public String suggestAMeal(int dishComplexity) {
        assert (1 <= dishComplexity && dishComplexity <= 5); //TODO: Move check to front-end
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
