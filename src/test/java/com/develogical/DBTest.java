package com.develogical;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import utilities.distributions.NormalDistr;

import java.util.ArrayList;
import java.util.List;

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
        final String meal = "cheeseburger";

        final DBController dbController = context.mock(DBController.class);

        context.repeat(1000, () -> {
                    context.checking(new Expectations() {{
                        exactly(1).of(dbController).lookupMealIngredients(meal);
                        will(returnValue(mealIngredients));
                        inTime(new NormalDistr(100, 10));
                        exactly(mealIngredients.size()).of(dbController).lookupIngredientNutrition(with(any(Ingredient.class)));
                        will(returnValue(200.0));
                        inTime(new NormalDistr(700, 10));
                    }});

                    new QueryProcessor(dbController).getNutritionalData(meal);
                });
        assertThat(context.getMultipleVirtualTimes(), hasPercentile(80, lessThan(8000.0)));
    }
}