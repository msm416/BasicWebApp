package com.develogical;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QueryProcessorTest {

    QueryProcessor queryProcessor = new QueryProcessor(new ActualDBController());

    @Test
    public void returnsEmptyStringIfCannotProcessQuery() throws Exception {
        assertThat(queryProcessor.getNutritionalData("!@#%$ASbnf"), is("nb of kcals in meal: 0.0"));
    }

    @Test
    public void knowsAboutRecipes() throws Exception {
        assertThat(queryProcessor.getNutritionalData("greek salad"), containsString("nb of kcals in meal: "));
    }

    @Test
    public void suggestingAMealWithComplexityMinimumReturnsSpam() throws Exception {
        assertThat(queryProcessor.suggestAMeal(1), is("canned spam"));
    }
}
