package com.develogical;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;
import umontreal.ssj.probdist.Distribution;
import umontreal.ssj.probdist.LaplaceDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.UniformIntDist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jmock.utils.LogsAndDistr.*;
import static org.junit.Assert.assertThat;
import static utilities.distributions.PerfStatistics.hasPercentile;

public class DBTest {

    static final ArrayList mealIngredients = new ArrayList() {{
        add(new Ingredient("ingr1", 100));
        add(new Ingredient("ingr2", 100));
        add(new Ingredient("ingr3", 100));
        add(new Ingredient("ingr4", 100));
        add(new Ingredient("ingr5", 100));
    }};

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
    }};;

    @Test
    public void getNutritionalDataForMeal() throws Exception {
        final DBController dbController = context.mock(DBController.class);

        final Distribution lookupMealIngredientsDistr = getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupMealIngredients"),
                "lookupMealIngredientsDistr");

        final Distribution lookupIngredientNutritionDistr = getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupIngredientNutrition"),
                "lookupIngredientNutritionDistr");

//        double adjFactor = getAdjustmentFactor(getBestDistributionFromEmpiricalData(
//                getSamplesFromLog("logs.txt", "lookupIngredientNutritionCombined"),
//                "lookupIngredientNutritionCombinedDistr"), getBestDistributionFromEmpiricalData(
//                getSamplesFromLog("logs.txt", "lookupIngredientNutritionCombinedParallel"),
//                "lookupIngredientNutritionCombinedParallelDistr"));
//
//        System.out.println("ADJ factor " + adjFactor);
        final Distribution nbOfCallsDist = new UniformIntDist(1, 5);

        final String meal = "cheeseburger";
        context.repeat(1000, () -> {

            int nbOfcalls = (int) nbOfCallsDist.inverseF(Math.random());

            context.checking(new Expectations() {{
                exactly(1).of(dbController).lookupMealIngredients(meal);
                will(returnValue(mealIngredients.subList(0, nbOfcalls)));
                //inTime(new NormalDist(50, 5));
                inTime(lookupMealIngredientsDistr);
                exactly(nbOfcalls).of(dbController).lookupIngredientNutrition(with(any(Ingredient.class)));
                will(returnValue(200.0));
                //inTime(new NormalDist(25, 5));
                inTime(lookupIngredientNutritionDistr
                        , 1.5
//                       , adjFactor
                );
            }});

            new QueryProcessor(dbController).getNutritionalData(meal);
        });

        assertThat(context.getMultipleVirtualTimes(false), hasPercentile(80, lessThan(200.0)));
    }

    @Test
    public void getNutritionalDataForSuggestedMeal() throws Exception {
        final DBController dbController = context.mock(DBController.class);

        final Distribution ltmbyDistr = getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupTopMealByComplexity"),
                "ltmbyDistr");
        //TODO: FACTOR in getSamplesFromLog i.e. my method is x2 times better than the data
        final int dishComplexity = 3;

        context.repeat(100, () -> {
            context.checking(new Expectations() {{
                exactly(1).of(dbController).lookupTopMealByComplexity(dishComplexity);
                will(returnValue("sampleMeal:ingr1, ingr2"));
                inTime(ltmbyDistr);
                atMost(5).of(dbController).lookupOnApiIngredientDetails(with(any(String.class)));
                //will(returnValue("nutritionalValue:..."));
                inTime(new LaplaceDist(360.0, 67.4));
            }});

            new QueryProcessor(dbController).suggestAMeal(dishComplexity);
        });
        assertThat(context.getMultipleVirtualTimes(false), hasPercentile(80, lessThan(1000.0)));
    }
}