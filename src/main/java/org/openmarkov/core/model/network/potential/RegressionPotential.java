package org.openmarkov.core.model.network.potential;

import java.util.List;
import java.util.Random;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.XORShiftRandom;

public abstract class RegressionPotential extends Potential {

    /**
     * Coefficients for the parameters of the function 
     */
    protected double[] coefficients;
    /**
     * Sampled coefficients
     */
    protected double[] sampledCoefficients;
    /**
     * Covariance matrix
     */
    protected double[] covarianceMatrix = null;
    /**
     * Colesky decomposition
     */
    protected double[] choleskyDecomposition         = null;

    public RegressionPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }

    public RegressionPotential(List<Variable> variables, PotentialRole role, double[] coefficients) {
        super(variables, role);
        this.coefficients = coefficients;
        this.sampledCoefficients = null;
    }

    public RegressionPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double[] covarianceMatrix) {
        this(variables, role, coefficients);
        this.covarianceMatrix = covarianceMatrix;
        this.choleskyDecomposition = calculateCholesky(covarianceMatrix);
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }

    public double getGamma() {
        return coefficients[0];
    }

    public void setGamma(double gamma) {
        this.coefficients[0] = gamma;
    }

    public double getConstant() {
        return coefficients[1];
    }

    public void setConstant(double constant) {
        this.coefficients[1] = constant;
    }

    public double[] getCovarianceMatrix() {
        return covarianceMatrix;
    }

    public void setCovarianceMatrix(double[] covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
        this.choleskyDecomposition = calculateCholesky(covarianceMatrix);
    }

    public double[] getCholeskyDecomposition() {
        return choleskyDecomposition;
    }
    
    @Override
    public boolean isUncertain() {
        return this.covarianceMatrix != null ||  this.choleskyDecomposition != null;
    }    

    
    
    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase,
            InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        double[] coefficients = (sampledCoefficients == null) ? this.coefficients
                : this.sampledCoefficients;
        return tableProject(evidenceCase, inferenceOptions, coefficients);
    }

    protected abstract List<TablePotential> tableProject(EvidenceCase evidenceCase,
            InferenceOptions inferenceOptions,
            double[] coefficients)
            throws NonProjectablePotentialException, WrongCriterionException;

    @Override
    public Potential sample() {
        if(covarianceMatrix != null)
        {
            if(this.sampledCoefficients == null)
            {
                this.sampledCoefficients = new double[coefficients.length];
            }
            
            Random randomGenerator = new XORShiftRandom();
            NormalFunction normalDistribution = new NormalFunction(0, 1); 
            double[] normalSamples = new double[coefficients.length];
            for(int i=0; i < normalSamples.length; ++i)
            {
                double sample = normalDistribution.getSample(randomGenerator);
                normalSamples[i] = sample;
            }
        
            int index = 0;
            for(int i=0; i < coefficients.length; ++i)
            {
                double value = 0.0;
                for(int j=0; j <= i; ++j)
                {
                    value += choleskyDecomposition[index] * normalSamples[j];
                    index++;
                }
                sampledCoefficients[i] = value + coefficients[i];
            }
        }
        return this;
    }    

    private static double[] calculateCholesky(double[] covarianceMatrix) {
        double[] cholesky = null;
        if (covarianceMatrix != null) {
            // Cholesky decomposition using the the Cholesky–Banachiewicz
            // algorithm
            cholesky = new double[covarianceMatrix.length];
            // Solve quadratic equation to get n, the number of coefficients
            int n = (int) (Math.sqrt(covarianceMatrix.length * 8 + 1) - 1) / 2;
            double[] diagonals = new double[n];
            int[] firstIndexOfRow = new int[n];
            int index = 0;
            for (int i = 0; i < n; ++i) {
                double sumOfSquares = 0.0;
                firstIndexOfRow[i] = index;
                for (int j = 0; j <= i; ++j) {
                    if (i == j) {
                        diagonals[i] = Math.sqrt(covarianceMatrix[index] - sumOfSquares);
                        cholesky[index] = diagonals[i];
                    } else {
                        double sumOfMul = 0.0;
                        for (int k = 0; k < j; ++k) {
                            sumOfMul += cholesky[firstIndexOfRow[i] + k]
                                    * cholesky[firstIndexOfRow[j] + k];
                        }
                        cholesky[index] = (covarianceMatrix[index] - sumOfMul) / diagonals[j];
                    }
                    sumOfSquares += Math.pow(cholesky[index], 2);
                    ++index;
                }
            }
        }
        return cholesky;
    }
}
