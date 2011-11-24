package org.openmarkov.core.model.network.modelUncertainty;

public class DirichletFunction extends ProbDensFunction {

		double alpha;
		
		public DirichletFunction(){
			super(TypeProbDensityFunction.DIRICHLET);
			
		}

		@Override
		public void placeParameters(Double[] params,boolean createSSJPDF) {
			// TODO Auto-generated method stub
			alpha = params[0];
		}

		@Override
		public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
			// TODO Auto-generated method stub
			return (alpha>0);
		}

		@Override
		public boolean isPossibleDistribution(boolean isChance) {
			// TODO Auto-generated method stub
			return isChance;
		}

		@Override
		public int getNumberOfRequiredArguments() {
			// TODO Auto-generated method stub
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

}
