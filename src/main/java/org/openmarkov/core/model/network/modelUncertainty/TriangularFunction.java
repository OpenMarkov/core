package org.openmarkov.core.model.network.modelUncertainty;

	
	public class TriangularFunction extends ProbDensFunctionWithKnownInverseCDF {
		
		/**
		 * Minimum
		 */
		double a;
		
		/**
		 * Maximum
		 */
		double b;
		/**
		 * Mode
		 */
		double c;
		

		public TriangularFunction(){
			super(TypeProbDensityFunction.TRIANGULAR);
			
		}

		@Override
		public void placeParameters(Double[] params) {

			a = params[0];
			b = params[1];
			c = params[2];
	
		}

		@Override
		public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		
			return (((0<=a)&&(a<=c)&&(c<=b)&&(b<=1)&&(a<b))&&isChanceVariable)||((a<=c)&&(c<=b)&&(a<b)&&!isChanceVariable);
		}

		@Override
		public boolean isPossibleDistribution(boolean isChance) {
			return true;
		}

		@Override
		public int getNumberOfRequiredArguments() {
			return 3;
		}
		
		@Override
		public double[] getParameters() {
			double[] x=new double[3];
			x[0]=a;
			x[1]=b;
			x[2]=c;
			return x;
		}
		
		@Override
		public double getMaximum() {
			
			return b;
		}
		
		@Override
		public double getMean() {
			return (a+b)/2;
		}
		
		@Override
		public double getInverseCumulativeDistributionFunction(double y) {
			double x;
			double sample;
			double diffBA;
						
			diffBA = b-a;
						
			x = a+ Math.sqrt(y*diffBA*(c-a));
			
			if (x<c){
				sample = x;
			}
			else{
				sample = b-Math.sqrt((1-y)*diffBA*(c-a)); 
			}
			return sample;
		}

	
	}

