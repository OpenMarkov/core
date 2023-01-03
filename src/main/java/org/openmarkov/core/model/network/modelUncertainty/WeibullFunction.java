package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

/**
 * This class represents a Weibull distribution
 * TODO introduce what we understand for lamda and k and describe the distribution
 *
 * @author cmyago
 * @version 1.0 - 19/03/2020 Adapted from ExponentialFunction
 */
@ProbDensFunctionType(name = "Weibull", isValidForProbabilities = false, parameters = {"lambda", "k"})
public class WeibullFunction extends ProbDensFunctionWithKnownInverseCDF {
    /**
     * Scale
     */
    private double lambda;

    /**
     * Shape
     */
    private double k;


    /**
     * Creates a WeibullFuncion object with scale lambda=0 and shape k=0
     */
    public WeibullFunction() {
        this(0.0, 0);
    }

    /**
     * Creates a WeibullFunction object with scale lambda and shape k
     *
     * @param lambda - scale parameter
     * @param k      - shape parameter
     */
    public WeibullFunction(double lambda, double k) {
        this.lambda = lambda;
        this.k = k;

    }

    /**
     * Creates a WeibullFuncion object equal to weibullFuncion
     *
     * @param weibullFunction Weibull Funcion use to create an equal WeibullFunction object
     */
    public WeibullFunction(WeibullFunction weibullFunction) {
        super();
        this.lambda = weibullFunction.lambda;
        this.k = weibullFunction.k;
    }

    /**
     * Returns the scale parameter (lambda) of this WeibullFuncion
     *
     * @return the scale parameter (lambda) of this WeibullFuncion
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Sets the scale parameter (lambda) of the WeibullFunction
     *
     * @param lambda the lambda to be set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }


    /**
     * Returns the shape parameter (k) of this WeibullFuncion
     * Returns the shape parameter (k) of this WeibullFuncion
     */
    public double getK() {
        return k;
    }


    /**
     * Sets the shape parameter (k) of this WeibullFuncion
     */
    public void setK(double k) {
        this.k = k;
    }


    /**
     * Returns an array of double where array[1] is lambda (scale) and array[0] is k (shape)
     *
     * @return an array of double where array[0] is lambda and array[1] is k
     */
    @Override
    public double[] getParameters() {
        double[] a = new double[2];
        a[0] = lambda;
        a[1] = k;
        return a;
    }

    /**
     * Sets params[1] as lamdba (scale) and params[0] as k (shape) in this WeibullFuncion
     *
     * @param params array of double where params[1]= lambda params[0]=k
     */
    @Override
    public void setParameters(double[] params) {
        lambda = params[0];
        k = params[1];
    }


    /**
     * Checks is the domain of the parameters is correct. In Weibull functions lambda and k should be greater than zero,
     * therefore eeturns true if lambda>0 and k >0
     *
     * @param isChanceVariable - not used, kept form compatibility
     * @return true
     */
    @Override
    public boolean verifyParametersDomain(boolean isChanceVariable) {
        return ((lambda > 0) && (k > 0));
    }


    /**
     * @param parameters - parameters[1]= mu and parameters[0] = sigma^2
     * @throws IllegalArgumentException - thrown if sigma<0
     */
    @Override
    public void verifyParameters(double[] parameters) {
        if (!(parameters[0] > 0)) {
            throw new IllegalArgumentException("Wrong parameters" + this.getClass().getName());
        }
    }


    /**
     * Returns the mean of this WeibullFunction distribution. The mean of a Weibull distribution is lambda*Gamma_Funcion(1+1/k)
     *
     * @return the mean of this WeibullFunction distribution.
     */
    @Override
    public double getMean() {
		/*Why not use Gamma.Gamma?
		In rg.apache.commons.math3.distribution.WeibullDistribution.java is calculated like that
		and I have replicated the code but I don't understand why they do not use Gammma.gamma
		*/
        return lambda * FastMath.exp(Gamma.logGamma(1 + (1 / k)));
    }


    /**
     * @return the
     * Returns the mean of this WeibullFunction distribution. The mean of a Weibull distribution is lambda*Gamma_Funcion(1+1/k)
     * * @return the mean of this WeibullFunction distribution.
     */
    @Override
    public double getVariance() {
        return (k * k) * FastMath.exp(Gamma.logGamma(1 + (2 / lambda))) -
                (getMean() * getMean());
    }

    /**
     * Returns the minimun of the support of the Weibull distribudion. The Weibull distribucion support is [0, +inf)
     *
     * @return 0
     */
    @Override
    public double getMinimum() {
        return 0;
    }


    /**
     * Returns the maximun of the support of the Weibull distribudion. The Weibull distribucion support is [0, +inf)
     *
     * @return Double.POSITIVE_INFINITY;
     */
    @Override
    public double getMaximum() {
        return Double.POSITIVE_INFINITY;
    }


    /**
     * Returns Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for a Weibull distribution
     *
     * @param y 0<=y<=1
     * @return Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for a Weibull distribution
     */
    @Override
    public double getInverseCumulativeDistributionFunction(double y) {

        double returnValue;
        if (y < 0.0 || y > 1.0) {
            throw new OutOfRangeException(y, 0.0, 1.0);
        } else if (y == 0) {
            returnValue = 0.0;
        } else if (y == 1) {
            returnValue = Double.POSITIVE_INFINITY;
        } else {
            returnValue = lambda * FastMath.pow(-FastMath.log1p(-y), 1.0 / k);
        }
        return returnValue;
    }


    /**
     * Returns a copy of this object
     *
     * @return a copy of this object
     */
    @Override
    public ProbDensFunction copy() {
        return new WeibullFunction(this);
    }


}
