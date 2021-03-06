/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.statistics.distribution;

import org.apache.commons.numbers.combinatorics.BinomialCoefficientDouble;
import org.apache.commons.numbers.combinatorics.LogBinomialCoefficient;
import org.apache.commons.numbers.gamma.RegularizedBeta;

/**
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Negative_binomial_distribution">Pascal distribution.</a>
 *
 * The Pascal distribution is a special case of the Negative Binomial distribution
 * where the number of successes parameter is an integer.
 *
 * There are various ways to express the probability mass and distribution
 * functions for the Pascal distribution. The present implementation represents
 * the distribution of the number of failures before {@code r} successes occur.
 * This is the convention adopted in e.g.
 * <a href="http://mathworld.wolfram.com/NegativeBinomialDistribution.html">MathWorld</a>,
 * but <em>not</em> in
 * <a href="http://en.wikipedia.org/wiki/Negative_binomial_distribution">Wikipedia</a>.
 *
 * For a random variable {@code X} whose values are distributed according to this
 * distribution, the probability mass function is given by<br>
 * {@code P(X = k) = C(k + r - 1, r - 1) * p^r * (1 - p)^k,}<br>
 * where {@code r} is the number of successes, {@code p} is the probability of
 * success, and {@code X} is the total number of failures. {@code C(n, k)} is
 * the binomial coefficient ({@code n} choose {@code k}). The mean and variance
 * of {@code X} are<br>
 * {@code E(X) = (1 - p) * r / p, var(X) = (1 - p) * r / p^2.}<br>
 * Finally, the cumulative distribution function is given by<br>
 * {@code P(X <= k) = I(p, r, k + 1)},
 * where I is the regularized incomplete Beta function.
 */
public class PascalDistribution extends AbstractDiscreteDistribution {
    /** The number of successes. */
    private final int numberOfSuccesses;
    /** The probability of success. */
    private final double probabilityOfSuccess;
    /** The value of {@code log(p)}, where {@code p} is the probability of success,
     * stored for faster computation. */
    private final double logProbabilityOfSuccess;
    /** The value of {@code log(1-p)}, where {@code p} is the probability of success,
     * stored for faster computation. */
    private final double log1mProbabilityOfSuccess;

    /**
     * Create a Pascal distribution with the given number of successes and
     * probability of success.
     *
     * @param r Number of successes.
     * @param p Probability of success.
     * @throws IllegalArgumentException if {@code r <= 0} or {@code p < 0}
     * or {@code p > 1}.
     */
    public PascalDistribution(int r,
                              double p) {
        if (r <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE,
                                            r);
        }
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.INVALID_PROBABILITY, p);
        }

        numberOfSuccesses = r;
        probabilityOfSuccess = p;
        logProbabilityOfSuccess = Math.log(p);
        log1mProbabilityOfSuccess = Math.log1p(-p);
    }

    /**
     * Access the number of successes for this distribution.
     *
     * @return the number of successes.
     */
    public int getNumberOfSuccesses() {
        return numberOfSuccesses;
    }

    /**
     * Access the probability of success for this distribution.
     *
     * @return the probability of success.
     */
    public double getProbabilityOfSuccess() {
        return probabilityOfSuccess;
    }

    /** {@inheritDoc} */
    @Override
    public double probability(int x) {
        double ret;
        if (x < 0) {
            ret = 0.0;
        } else {
            ret = BinomialCoefficientDouble.value(x +
                  numberOfSuccesses - 1, numberOfSuccesses - 1) *
                  Math.pow(probabilityOfSuccess, numberOfSuccesses) *
                  Math.pow(1.0 - probabilityOfSuccess, x);
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public double logProbability(int x) {
        double ret;
        if (x < 0) {
            ret = Double.NEGATIVE_INFINITY;
        } else {
            ret = LogBinomialCoefficient.value(x +
                  numberOfSuccesses - 1, numberOfSuccesses - 1) +
                  logProbabilityOfSuccess * numberOfSuccesses +
                  log1mProbabilityOfSuccess * x;
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(int x) {
        double ret;
        if (x < 0) {
            ret = 0.0;
        } else {
            ret = RegularizedBeta.value(probabilityOfSuccess,
                                        numberOfSuccesses, x + 1.0);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * For number of successes {@code r} and probability of success {@code p},
     * the mean is {@code r * (1 - p) / p}.
     */
    @Override
    public double getMean() {
        final double p = getProbabilityOfSuccess();
        final double r = getNumberOfSuccesses();
        return (r * (1 - p)) / p;
    }

    /**
     * {@inheritDoc}
     *
     * For number of successes {@code r} and probability of success {@code p},
     * the variance is {@code r * (1 - p) / p^2}.
     */
    @Override
    public double getVariance() {
        final double p = getProbabilityOfSuccess();
        final double r = getNumberOfSuccesses();
        return r * (1 - p) / (p * p);
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    @Override
    public int getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity no matter the
     * parameters. Positive infinity is symbolized by {@code Integer.MAX_VALUE}.
     *
     * @return upper bound of the support (always {@code Integer.MAX_VALUE}
     * for positive infinity)
     */
    @Override
    public int getSupportUpperBound() {
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSupportConnected() {
        return true;
    }
}
