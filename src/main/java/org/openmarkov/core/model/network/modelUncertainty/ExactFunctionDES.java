/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author cyago
 * @version 1.0 - 15/08/2022 copied from ExactFunction for testing nuisance variance in DESnets FIXME remove and use ExactFunction
 *
 */
@ProbDensFunctionType(name = "ExactDES", isValidForNumeric = true, parameters = { "nu" })
public class ExactFunctionDES extends ProbDensFunctionWithKnownInverseCDF {
	private double nu;

	public ExactFunctionDES() {
	}

	public ExactFunctionDES(double nu) {
		this.nu = nu;
	}

	public ExactFunctionDES(ExactFunctionDES exactFunction) {
		super();
		this.nu = exactFunction.nu;
	}

	public double getNu() {
		return nu;
	}

	//CMI
	//For Univariate
	@Override public void verifyParameters(double[] parameters) throws IllegalArgumentException {
		//Parameters are always ok
	}

	//CMF
	@Override public boolean verifyParametersDomain(boolean isChanceVariable) {
		return ((!isChanceVariable) || ((0 <= nu) && (nu <= 1)));
	}

	/**
	 * Some subclasses can override this method.
	 *
	 * @return
	 */
	public double getMean() {
		return nu;
	}

	@Override public double[] getParameters() {
		double[] a = new double[1];
		a[0] = nu;
		return a;
	}

	@Override public void setParameters(double[] params) {
		nu = params[0];
	}

	@Override public double getMaximum() {
		return nu;
	}


	@Override public double getVariance() {
		return 0;
	}

	@Override public double getMinimum() {
		return nu;
	}

	@Override public DomainInterval getInterval(double p) {
		return new DomainInterval(nu, nu);
	}

	@Override
	public double getInverseCumulativeDistributionFunction(double y) {
		return nu;
	}

	@Override public ProbDensFunction copy() {
		return new ExactFunctionDES(this);
	}
}
