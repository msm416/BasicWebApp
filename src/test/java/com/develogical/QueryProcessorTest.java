package com.develogical;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QueryProcessorTest {

    QueryProcessor queryProcessor = new QueryProcessor(new ActualDBController());

    @Test
    public void returnsEmptyStringIfCannotProcessQuery() throws Exception {
        assertThat(queryProcessor.getNutritionalData("!@#%$ASbnf"), is("db lookup result: 0.0"));
    }

    @Test
    public void knowsAboutRecipes() throws Exception {
        assertThat(queryProcessor.getNutritionalData("greek salad"), containsString("db lookup result"));
    }
}
