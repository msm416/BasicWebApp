package com.develogical;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class DELETDBTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery() {{
//        setThreadingPolicy(new Synchroniser());
    }};

    @Test
    public void getNutritionalDataForMeal() {

        final ArrayList mealIngredients = new ArrayList() {{
            add(new DELETIngredient());
            add(new DELETIngredient());
            add(new DELETIngredient());
            add(new DELETIngredient());
            add(new DELETIngredient());
        }};

        final DELETDBController dbController = context.mock(DELETDBController.class);

        final String meal = "cheeseburger";

        context.checking(new Expectations() {{
            exactly(1).of(dbController).lookupMealIngredients(meal);
            will(returnValue(mealIngredients));
            exactly(mealIngredients.size()).of(dbController).lookupIngredientNutrition(with(any(DELETIngredient.class)));
            //will(returnValue(200.0));
        }});

        String nutritionalData = (new DELETQueryProcessor(dbController).getNutritionalData(meal));
    }
}
