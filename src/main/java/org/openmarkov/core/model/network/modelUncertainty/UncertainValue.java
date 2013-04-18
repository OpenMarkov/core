/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * A <code>ExtendedValue</code> is a value of a table of potentials which is
 * used for sensitivity analysis.
 * 
 * @author Manuel Luque
 * @author Elena Almaraz
 * @author Javier Diez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public class UncertainValue {
	// Attributes
	/** Arguments of the distribution. */
	protected String arguments;
	/** Probability density function. */
	protected ProbDensFunction probDensFunction;
	protected boolean correctArgumentsInProbDensFunction;
	/** Name of the parameter. */
	protected String name;

	public UncertainValue(double value) {
		name = null;
		arguments = Double.toString(value);
		probDensFunction = new ExactFunction();
	}

	public UncertainValue(TypeProbDensityFunction type, String arguments,
			String name) {
		this.name = name;
		this.arguments = arguments;
		ProbDensFunction auxProb;
		auxProb = ProbDensFunction.constructNewProbDensFunction(type);
		String[] args = splitArgumentsInListOfDouble();
		correctArgumentsInProbDensFunction = (args.length == ProbDensFunction
				.getNumberOfRequiredArguments(type));
		if (!correctArgumentsInProbDensFunction) {
			System.out.println("Error in the number of arguments.");
			this.probDensFunction = null;
		} else {
			auxProb.placeParameters(args);
		}
		this.probDensFunction = auxProb;
	}
	
	
	public UncertainValue(TypeProbDensityFunction type, Double[] arguments,
			String name) {
		this.name = name;
		ProbDensFunction auxProb;
		auxProb = ProbDensFunction.constructNewProbDensFunction(type);
		auxProb.placeParameters(arguments);
		this.probDensFunction = auxProb;
	}
	
	public UncertainValue(TypeProbDensityFunction type, Double[] arguments) {
		this(type, arguments, null);
	}

	public UncertainValue(TypeProbDensityFunction type, String arguments) {
		this(type, arguments, null);
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCorrectArgumentsInProbDensFunction() {
		return correctArgumentsInProbDensFunction;
	}

	public void setCorrectArgumentsInProbDensFunction(
			boolean correctArgumentsInProbDensFunction) {
		this.correctArgumentsInProbDensFunction = correctArgumentsInProbDensFunction;
	}

	public ProbDensFunction getProbDensityFunction() {
		return probDensFunction;
	}

	public void setProbDensityFunction(ProbDensFunction probDensityFunction) {
		this.probDensFunction = probDensityFunction;
	}

	public boolean hasProbabilisticDensityFunction() {
		return probDensFunction != null;
	}

	/*
	 * public ArrayList<String> splitArgumentsInListOfDouble(){ int size =
	 * arguments.length(); ArrayList<String> args; args = new
	 * ArrayList<String>(); if (size>0){ boolean notSeparator = true; int i=0;
	 * while (i<size){ int j; for (j=i;j<size&&notSeparator;j++){ notSeparator =
	 * isCharacterAllowedInNumber(arguments.substring(j,j+1)); } if
	 * (!notSeparator){ args.add(arguments.substring(i,j)); i=j; } } } return
	 * args; }
	 */
	public String[] splitArgumentsInListOfDouble() {
		// String patternSeparator = " ";
		// We use as separator an unlimited sequence of comma, semicolon and
		// white spaces.
		String patternSeparator = "[,; ]+";
		String[] args = Pattern.compile(patternSeparator).split(arguments);
		return args;
	}

	/*
	 * private static boolean isCharacterAllowedInNumber(String c) {
	 * Auto-generated method stub String allowed = "1234567890+-e"; return
	 * (allowed.indexOf(c)!=-1); }
	 */
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return probDensFunction
				.doParametersVerifyDomainConstraint(isChanceVariable);
	}

	public double getSample(Random randomGenerator) {
		return probDensFunction.getSample(randomGenerator);
	}
	/*
	 * public boolean isComplementOfOtherValues() { return
	 * isComplementOfOtherValues; } public void
	 * setComplementOfOtherValues(boolean isComplementOfOtherValues) {
	 * this.isComplementOfOtherValues = isComplementOfOtherValues; } public
	 * double getNumericValue() { return numericValue; } public void
	 * setNumericValue(double numericValue) { this.numericValue = numericValue;
	 * }
	 */
}
