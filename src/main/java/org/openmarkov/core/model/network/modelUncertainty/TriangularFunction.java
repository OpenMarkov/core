package org.openmarkov.core.model.network.modelUncertainty;

import umontreal.iro.lecuyer.probdist.TriangularDist;
import umontreal.iro.lecuyer.randvar.TriangularGen;

	
	public class TriangularFunction extends ProbDensFunction {
		
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
		public void placeParameters(Double[] params,boolean createSSJPDF) {

			a = params[0];
			b = params[1];
			c = params[2];
			if (createSSJPDF){
				createSSJPDF();
			}
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
		public void createSSJPDF() {
			ssjPDF = new TriangularDist(a,b,c);
			
		}


		@Override
		public void initializeGenerator() {
			generator = new TriangularGen(stream,(TriangularDist)ssjPDF);
		}
		


	}

