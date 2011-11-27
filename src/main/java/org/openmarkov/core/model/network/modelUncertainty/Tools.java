package org.openmarkov.core.model.network.modelUncertainty;

public class Tools {
	   public static double sum(double[] aa){
           double sum=0.0D;
           for(int i=0; i<aa.length; i++){
                   sum+=aa[i];
           }
           return sum;
	   }
	   
	   public static double[] normalize(double []x,double mass){
		   
		   double[] y;
		   double divisor;
			
			int length = x.length;
			y = new double[length];
			
			double sum;
			
			sum = sum(x);
			divisor = sum/mass;
			for (int i=0;i<length;i++){
				y[i] = x[i]/divisor; 
			}
			return y;
	   }
	   
	   public static double[] normalize(double []x){
		   
		   return normalize(x,1.0);
	   }
	   
	   public static double square(double x){
		   return Math.pow(x, 2.0);
	   }
}
