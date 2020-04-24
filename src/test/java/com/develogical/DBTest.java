package com.develogical;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import umontreal.ssj.probdist.NormalDist;

import java.util.ArrayList;

import static com.develogical.GeneratePerfLogs.getBestDistributionFromEmpiricalData;
import static com.develogical.GeneratePerfLogs.getSamplesFromLog;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;
import static utilities.distributions.PerfStatistics.hasPercentile;

public class DBTest {

    static final ArrayList mealIngredients = new ArrayList() {{
        add(new Ingredient("ingr1", 100));
        add(new Ingredient("ingr2", 100));
        add(new Ingredient("ingr3", 100));
        add(new Ingredient("ingr4", 100));
        add(new Ingredient("ingr5", 100));
        add(new Ingredient("ingr6", 100));
        add(new Ingredient("ingr7", 100));
        add(new Ingredient("ingr8", 100));
        add(new Ingredient("ingr9", 100));
        add(new Ingredient("ingr10", 100));
        add(new Ingredient("ingr11", 100));
    }};

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void getNutritionalDataForMeal() {
        System.out.println();
        final String meal = "cheeseburger";

        final DBController dbController = context.mock(DBController.class);

        context.repeat(1000, () -> {
                    context.checking(new Expectations() {{
                        exactly(1).of(dbController).lookupMealIngredients(meal);
                        will(returnValue(mealIngredients));
                        inTime(getBestDistributionFromEmpiricalData(
                                getSamplesFromLog("logs.txt", "lookupMealIngredients")));
                        exactly(mealIngredients.size()).of(dbController).lookupIngredientNutrition(with(any(Ingredient.class)));
                        will(returnValue(200.0));
                        inTime(new NormalDist(100, 10));
                    }});

                    new QueryProcessor(dbController).getNutritionalData(meal);
                });
        assertThat(context.getMultipleVirtualTimes(), hasPercentile(80, lessThan(8000.0)));
    }

    @Test
    public void getNutritionalDataForSuggestedMeal() {
        final DBController dbController = context.mock(DBController.class);
        final int dishComplexity = 3;
        context.repeat(1000, () -> {
            context.checking(new Expectations() {{
                exactly(1).of(dbController).lookupTopMealByComplexity(dishComplexity);
                will(returnValue("sampleMeal:ingr1, ingr2"));
                inTime(getBestDistributionFromEmpiricalData(
                        getSamplesFromLog("logs.txt", "lookupTopMealByComplexity")));
                atMost(5).of(dbController).lookupOnApiIngredientDetails(with(any(String.class)));
                //will(returnValue("nutritionalValue:..."));
                inTime(new NormalDist(700, 10));
            }});

            new QueryProcessor(dbController).suggestAMeal(dishComplexity);
        });
        assertThat(context.getMultipleVirtualTimes(), hasPercentile(80, lessThan(2000.0)));
    }
}