package org.openmarkov.core.model.network.modelUncertainty;

public class DirichletFunction extends ProbDensFunction {

		double alpha;
		
		public DirichletFunction(){
			super(TypeProbDensityFunction.DIRICHLET);
			
		}

		@Override
		public void placeParameters(Double[] params) {
			alpha = params[0];
		}

		@Override
		public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
			return (alpha>0);
		}

		@Override
		public boolean isPossibleDistribution(boolean isChance) {
			return isChance;
		}

		@Override
		public int getNumberOfRequiredArguments() {
			return 1;
		}

		@Override
		public double[] getParameters() {
			double[]a = new double[1];
			a[0]=alpha;
			return a;
		}

		@Override
		public double getMaximum() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public double getMean() {
			return alpha;
		}

		@Override
		public double getSample() {
			return 0;
		}

		@Override
		public double getVariance() {
			// TODO Auto-generated method stub
			return 0;
		}

}
