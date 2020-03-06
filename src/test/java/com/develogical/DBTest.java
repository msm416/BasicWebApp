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

public class DBTest {

    final List<Ingredient> mealIngredients = new ArrayList<>();

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void lookupRecipeArticleInDB() {
        final String recipeName = "how to craft an iron sword";

        final DBController dbController = context.mock(DBController.class);

        context.checking(new Expectations() {{
            exactly(1).of(dbController).lookupMealIngredients(recipeName);
            will(returnValue(mealIngredients));
            inTime(new NormalDistr(100, 10));
        }});

        long startTime = System.currentTimeMillis();
        new QueryProcessor(dbController).getNutritionalData(recipeName);
        long endTime = System.currentTimeMillis();

        assertThat(context.getSingleVirtualTime(true)
                        + (endTime - startTime)
                        - context.getSingleRealTime(),
                lessThan(200.0));
    }
}