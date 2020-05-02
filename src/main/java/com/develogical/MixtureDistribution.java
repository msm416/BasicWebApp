package com.develogical;

import umontreal.ssj.probdist.ContinuousDistribution;

import java.util.*;

public class MixtureDistribution extends ContinuousDistribution {

    protected double[] weights;
    protected ContinuousDistribution[] distributions;

    public MixtureDistribution(double[] weights, ContinuousDistribution[] distributions) {
        if (weights.length != distributions.length || weights.length == 0) {
            throw new IllegalArgumentException("Arrays must match length and have size >=1: weights[] length is "
                    + weights.length + " and distributions[] length is " + distributions.length);
        }

        double weightSum = Arrays.stream(weights).sum();
        if (weightSum != 1.0) {
            throw new IllegalArgumentException("Weights must add up to exactly 1.0, but sum is " + weightSum);
        }

        this.weights = weights;
        this.distributions = distributions;
    }

    public MixtureDistribution(ContinuousDistribution[] distributions) {
        this.distributions = distributions;
    }

    // Heuristic to find a locally-good mixture distribution
    public static MixtureDistribution findBestMixedDistribution(double[] data,
                                                                List<ContinuousDistribution> distributions) {
        int runs = (int) Math.pow(2, distributions.size());

        double[][] weights = new double[runs+1][distributions.size()];

        double toleranceVal = 0.01;

        Random r = new Random();

        for (int i = 0; i < runs; i++) {
            List<Integer> indices = new ArrayList<>();
            for (int j = 0; j < distributions.size(); j++) {
                indices.add(j);
            }

            double remainingWeight = 1.0;

            while (indices.size() > 1) {
                if (remainingWeight < toleranceVal) {
                    // that should be sufficient since array is initialized with 0.0
                    break;
                }
                int index = indices.remove(r.nextInt(indices.size()));
                double getWeight = r.nextDouble() * remainingWeight;
                weights[i][index] = getWeight;
                remainingWeight -= getWeight;
            }
            weights[i][indices.remove(0)] = remainingWeight;
            for (int j = 0; j < distributions.size(); j++) {
            }
        }

        // Consider equally weighted distributions - which is often the case
        // i.e. adding three normal distributions
        for(int j = 0; j < distributions.size(); j++) {
            weights[runs][j] = 1.0/distributions.size();
        }

        List<ContinuousDistribution> mixtureDistributionList = new ArrayList<>();

        for(int i = 0; i < runs+1; i++) {
            mixtureDistributionList.add(new MixtureDistribution(weights[i],
                    distributions.toArray(new ContinuousDistribution[0])));
        }

        return (MixtureDistribution)
                (GeneratePerfLogs.getBestDistributionViaGoodnessToFitTest(data, mixtureDistributionList));

    }

    //TODO BUT NOT USED:
    @Override
    public double density(double v) {
        return 0;
    }

    @Override
    public double cdf(double v) {
        double cdf = 0.0;
        for(int i = 0; i < distributions.length; i++) {
            cdf += distributions[i].cdf(v) * weights[i];
        }
        return cdf;
    }

    //TODO BUT NOT USED:
    @Override
    public double[] getParams() {
        return new double[0];
    }
}
