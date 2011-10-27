package org.openmarkov.core.model.network;

import java.util.regex.Pattern;

import org.openmarkov.core.model.network.sensitivityAnalysis.ExactFunction;
import org.openmarkov.core.model.network.sensitivityAnalysis.ProbDensFunction;
import org.openmarkov.core.model.network.sensitivityAnalysis.TypeProbDensityFunction;
/*import openmarkov.networks.sensitivityanalysis.GammaFunction;
import openmarkov.networks.sensitivityanalysis.GammamvFunction;
import openmarkov.networks.sensitivityanalysis.LogNormalFunction;
import openmarkov.networks.sensitivityanalysis.NormalFunction;
import openmarkov.networks.sensitivityanalysis.RangeFunction;
import openmarkov.networks.sensitivityanalysis.TriangularFunction;
import openmarkov.networks.sensitivityanalysis.TypeProbDensityFunction;


/** A <code>ExtendedValue</code> is a value of a table of potentials which is used
 * for sensitivity analysis.
 * @author Manuel Luque
 * @author Elena Almaraz
 * @author Javier Diez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class UncertainValue {
	
	// Attributes
	/** Arguments of the distribution. */
	protected String arguments;
	

	public String getArguments() {
		return arguments;
	}


	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/** Name of the parameter. */
    protected String name;
    
    public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	/** Probability density function. */
    protected ProbDensFunction probDensFunction;
    
    boolean correctArgumentsInProbDensFunction;
    

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
	
	public boolean hasProbabilisticDensityFunction(){
		return probDensFunction!=null;
	}


	public UncertainValue(double value) {
		name = null;
		arguments = Double.toString(value);
		probDensFunction = new ExactFunction();
		// TODO Auto-generated constructor stub
	}
	
	public UncertainValue(TypeProbDensityFunction type,String arguments,String name,boolean createSSJPDF,boolean createRandomGenerator) {
		   
		this.name = name;
		this.arguments = arguments;
		ProbDensFunction auxProb;
		auxProb = ProbDensFunction.constructNewProbDensFunction(type);
		
		String[] args = splitArgumentsInListOfDouble();
		
		if (args.length!=ProbDensFunction.getNumberOfRequiredArguments(type)){
			 System.out.println("Error en el número de argumentos.");
			 correctArgumentsInProbDensFunction = false;
			 this.probDensFunction = null;
		}
		else{
			correctArgumentsInProbDensFunction = true;
			auxProb.placeParameters(args);
			if (createSSJPDF){
				auxProb.createSSJPDF();
			}
			if (createRandomGenerator){
				auxProb.createRandomGenerator();
			}
				
		}
		this.probDensFunction = auxProb;
		
	}
	
	
	
	public UncertainValue(TypeProbDensityFunction type,String arguments) {
		this(type,arguments,null,false,false);   
		
		
	}


	
	/*public ArrayList<String> splitArgumentsInListOfDouble(){
		 int size = arguments.length();
		 ArrayList<String> args;
		 
		 args = new ArrayList<String>();
		 
		 if (size>0){
			 boolean notSeparator = true;
			 int i=0;
			 while (i<size){
				 int j;
			 	 for (j=i;j<size&&notSeparator;j++){
			 		 notSeparator = isCharacterAllowedInNumber(arguments.substring(j,j+1));
			 	 }
			 	 if (!notSeparator){
			 		 args.add(arguments.substring(i,j));
			 		 i=j;				 		 
			 	 }
			 }
		 }
		 return args;
	  }*/
	
	
	public String[] splitArgumentsInListOfDouble(){
		 //String patternSeparator = " ";
		//We use as separator an unlimited sequence of comma, semicolon and white spaces.
		 String patternSeparator = "[,; ]+";
		 String[] args = Pattern.compile(patternSeparator).split(arguments);
		 return args;
	  }
	 
	/* private static boolean isCharacterAllowedInNumber(String c) {
			// TODO Auto-generated method stub
			String allowed = "1234567890+-e";
			return (allowed.indexOf(c)!=-1);
		}
*/

	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		// TODO Auto-generated method stub
		return probDensFunction.doParametersVerifyDomainConstraint(isChanceVariable);
	}


	public double getSample() {
		// TODO Auto-generated method stub
		return probDensFunction.getSample();
	}


	
	public void initializeGenerator() {
		// TODO Auto-generated method stub
		probDensFunction.initializeGenerator();
	}


	public void createRandomGenerator() {
		// TODO Auto-generated method stub
		probDensFunction.createRandomGenerator();
		
	}
        
	/*public boolean isComplementOfOtherValues() {
		return isComplementOfOtherValues;
	}

	public void setComplementOfOtherValues(boolean isComplementOfOtherValues) {
		this.isComplementOfOtherValues = isComplementOfOtherValues;
	}


    public double getNumericValue() {
		return numericValue;
	}


	public void setNumericValue(double numericValue) {
		this.numericValue = numericValue;
	}*/
    
    
    

}
