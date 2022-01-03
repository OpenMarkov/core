/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

/**
 * 04/05/2021
 */
@ProbDensFunctionType(name = "Truncated Normal", isValidForProbabilities = false, parameters = { "mu",
		"sigma" })
public class TruncatedNormalFunction extends NormalFunction {


	public TruncatedNormalFunction() {
		this(0.0, 1.0);
	}

	public TruncatedNormalFunction(double mu, double sigma) {
		super(mu, sigma);

	}

	public TruncatedNormalFunction(TruncatedNormalFunction normalFunction) {
		super(normalFunction);
	}

	@Override public double getSample(Random randomGenerator) {
		double value = super.getSample(randomGenerator);
		return (value>0)? value: 0;
	}

	@Override public ProbDensFunction copy() {
		return new TruncatedNormalFunction(this);
	}
}
