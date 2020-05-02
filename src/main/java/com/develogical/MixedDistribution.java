package com.develogical;

import umontreal.ssj.probdist.ContinuousDistribution;
import umontreal.ssj.probdist.Distribution;

import java.util.*;

public class MixedDistribution extends ContinuousDistribution {

    protected double[] weights;
    protected ContinuousDistribution[] distributions;

    public MixedDistribution(double[] weights, ContinuousDistribution[] distributions) {
        if(weights.length != distributions.length || weights.length==0) {
            throw new IllegalArgumentException("Arrays must match length and have size >=1: weights[] length is "
                    + weights.length + " and distributions[] length is " + distributions.length);
        }

        double weightSum = Arrays.stream(weights).sum();
        if(weightSum != 1.0) {
            throw new IllegalArgumentException("Weights must add up to exactly 1.0, but sum is " + weightSum);
        }

        this.weights = weights;
        this.distributions = distributions;
    }

    public MixedDistribution(ContinuousDistribution[] distributions) {
        this.distributions = distributions;
    }

    public static MixedDistribution findBestMixedDistribution(ContinuousDistribution[] distributions) {
        int runs =(int)Math.pow(2,distributions.length);

        double[][] weights = new double[runs][distributions.length];

        MixedDistribution[] mixedDistributions = new MixedDistribution[runs];

        double toleranceVal = 0.01;

        Random r = new Random();

        for(int i = 0; i < runs; i++) {
            List<Integer> indices = new ArrayList<>();
            for(int j = 0; j < distributions.length; j++) {
                indices.add(j);
            }

            double remainingWeight = 1.0;

            while(indices.size() > 1) {
                if(remainingWeight < toleranceVal) {
                    // that should be sufficient since array is initialized with 0.0
                    break;
                }
                int index = indices.remove(r.nextInt(indices.size()));
                double getWeight = r.nextDouble() * remainingWeight;
                weights[i][index] = getWeight;
                remainingWeight -= getWeight;
            }
            weights[i][indices.remove(0)] = remainingWeight;
            for(int j = 0; j < distributions.length; j++) {
                System.out.print(weights[i][j] + " ");
            }
            System.out.println();
        }

        return null;
    }

    @Override
    public double density(double v) {
        return 0;
    }

    @Override
    public double cdf(double v) {
        //TODO: weighted CDF
        return 0;
    }

    @Override
    public double[] getParams() {
        return new double[0];
    }
}
