package com.develogical;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.utils.SequentialCallsDist;
import org.junit.Rule;
import org.junit.Test;
import umontreal.ssj.probdist.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jmock.utils.LogsAndDistr.*;
import static org.jmock.utils.PerfStatistics.hasPercentile;
import static org.junit.Assert.assertThat;

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
    }};

    @Test
    public void getNutritionalDataForMeal() throws Exception {
        final DBController dbController = context.mock(DBController.class);

        final Distribution lookupMealIngredientsDistr = getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupMealIngredients", 0.2),
                "lookupMealIngredientsDistr");

        final Distribution lookupIngredientNutritionDistr = getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupIngredientNutrition", 0.2),
                "lookupIngredientNutritionDistr");

        double adjFactor = computeAdjustmentFactor(

                new SequentialCallsDist(new UniformIntDist(1, 5), lookupIngredientNutritionDistr),

                getBestDistributionFromEmpiricalData(
                getSamplesFromLog("logs.txt", "lookupIngredientNutritionCombinedParallel", 0d),
                        "lookupIngredientNutritionCombinedParallelDistr"));

        final Distribution nbOfCallsDist = new UniformIntDist(1, 5);

        final String meal = "cheeseburger";
        context.repeat(1000, () -> {

            int nbOfcalls = (int) nbOfCallsDist.inverseF(Math.random());
//            int nbOfcalls = 5;

            context.checking(new Expectations() {{
                exactly(1).of(dbController).lookupMealIngredients(meal);
                will(returnValue(mealIngredients.subList(0, nbOfcalls)));
//                inTime(new NormalDist(25, 5));
                inTime(lookupMealIngredientsDistr);

                exactly(nbOfcalls).of(dbController).lookupIngredientNutrition(with(any(Ingredient.class)));
//                inTime(new NormalDist(25, 5));
                inTime(lookupIngredientNutritionDistr
                       , adjFactor
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
                getSamplesFromLog("logs.txt", "lookupTopMealByComplexity", 0.2),
                "ltmbyDistr");
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

    public static DiscreteDistribution getDiscreteDistribution(double[] samples) {
        //PRE: samples are ordered and positive
        //POST: return a discrete distribution from samples

        int n = samples.length;

        int index = -1;

        double prevVal = -1;

        double[] prob = new double[n];

        double[] newSamples = new double[n];

        for(int i = 0; i < n; i++) {
            if(prevVal != samples[i]) {
                index++;
                newSamples[index] = samples[i];
            }

            prob[index] += 1d/n;

            prevVal = samples[i];
        }

        return new DiscreteDistribution(newSamples, prob, index + 1);
    }

    @Test
    public void testAccuracyDiscreteContinuous() throws Exception {
        String[] methods = new String[] {"lookupIngredientNutrition",
        "lookupOnApiIngredientDetails", };
        for(int j = 0; j < methods.length; j++) {
            String methodName = methods[j];

            int repetitions = 1000;

            double[] samples = getSamplesFromLog("logs.txt", methodName, 0d);

            double[] contDistSamples = new double[repetitions];

            double[] discreteDistSamples = new double[repetitions];

            final ContinuousDistribution continuousDist = (ContinuousDistribution) getBestDistributionFromEmpiricalData(
                    getSamplesFromLog("logs.txt", methodName, 0d), "contDist");

            final DiscreteDistribution discreteDist = getDiscreteDistribution(
                    getSamplesFromLog("logs.txt", methodName, 0d));

            for (int i = 0; i < repetitions; i++) {
                // not allowed to sample negative virtual times
                double contDistSam = 0d;
                while(contDistSam <= 0) {
                    contDistSam = continuousDist.inverseF(Math.random());
                }
                contDistSamples[i] = contDistSam;
                discreteDistSamples[i] = discreteDist.inverseF(Math.random());
            }

            Arrays.sort(contDistSamples);
            Arrays.sort(discreteDistSamples);

            for(double k = 0d; k <= 1d; k+= 0.2) {

                System.out.println("Real data             " + k + " percentile sample:" + samples[(int) ((samples.length-1) * k)]);
                System.out.println("GoF best distribution " + k + " percentile sample:" + contDistSamples[(int)((repetitions-1) * k)]);
                System.out.println("Discrete distribution " + k + " percentile sample:" + discreteDistSamples[(int)((repetitions-1) * k)]);
                System.out.println();
            }
        }
    }
}