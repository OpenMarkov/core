package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.List;


import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;



@PotentialType(name = "UnivariateDistr")
public class UnivariateDistrPotential extends Potential{
	   protected TablePotential distributionTable;
	    /** 
	     * Represents the probability distribution of the values of the table. It is internally described as a finite-states
	     * variable whose states are given by the parameters of the distribution
	     * */
	    private Class<? extends ProbDensFunction> probDensFunctionClass;
	    private String probDensFunctionName;
	    private String probDensFunctionUnivariateName;
	    private String probDensFunctionParametrizationName;
	    private String[] probDensFunctionParametersName;
	    private ProbDensFunctionManager probDensFunctionManager;
	    
	    protected Variable pseudoVariableDistribution;
	    public static final String PSEUDO_VARIABLE="pseudoVariableDistributionName"; 
	    
	    /**
	     * 
	     * @param potentialVariables
	     * @param role
	     */

	    public UnivariateDistrPotential(List<Variable> potentialVariables, PotentialRole role ) {
	        super(potentialVariables, role);
	        if(this.role == null) {
	            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
	        }
	        setProbDensFunctionClass(ExactFunction.class);
	        setDistributionTable(potentialVariables, role);
	        
	    }

	    /** 
	     * Now I still do not use parameterization
	     * @param potentialVariables
	     * @param name
	     * @param parametrization
	     * @param role
	     */
	    public UnivariateDistrPotential(List<Variable> potentialVariables, String name, String parametrization,  PotentialRole role ) throws InstantiationException {
	        super(potentialVariables, role);
	        if(this.role == null) {
	            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
	        }
	        
	        setProbDensFunctionClass(getProbDensFunction(name, parametrization));
	        setDistributionTable(potentialVariables, role);
	    }
	    
	    /**
	     * 
	     * @param variables
	     * @param probDensFunctionClass
	     * @param role
	     */
	    public UnivariateDistrPotential(List<Variable> potentialVariables, Class<? extends ProbDensFunction> probDensFunctionClass, PotentialRole role ) {
	        super(potentialVariables, role);
	        if(this.role == null) {
	            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
	        }
	        setProbDensFunctionClass(probDensFunctionClass);
	        setDistributionTable(potentialVariables, role);
	    }

	    /**
	     * 
	     * @param potential
	     */
	    public UnivariateDistrPotential(UnivariateDistrPotential potential) {
	    	
	    	super(potential);
	    	setProbDensFunctionClass(potential.getProbDensFunctionClass());
	    	setDistributionTable((TablePotential)potential.getDistributionTable().copy());
	    	
	    
	    }  
	    
	    /**
	     * 
	     * @param variables
	     */
	    public UnivariateDistrPotential(List<Variable> variables) {
	        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
	    }

	    
	    /**
		 * @return the probDensFunctionManager
		 */
		public ProbDensFunctionManager getProbDensFunctionManager() {
			if (probDensFunctionManager==null) {
				probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
			}
			return probDensFunctionManager;
		}

		/**
		 * @param probDensFunctionManager the probDensFunctionManager to set
		 */
		public void setProbDensFunctionManager(ProbDensFunctionManager probDensFunctionManager) {
			this.probDensFunctionManager = probDensFunctionManager;
		}

		public Class<? extends ProbDensFunction> getProbDensFunction(String univariateName, String parametrization) throws InstantiationException {
			
			return getProbDensFunctionManager().getProbDensFunctionClass(univariateName, parametrization);
	    }
	    
	    /**
		 * @return the distribution
		 */
		public Class<? extends ProbDensFunction> getProbDensFunctionClass() {
			return probDensFunctionClass;
		}

		/**
		 * @param distributionClass the distribution to set
		 */
		public void setProbDensFunctionClass(Class<? extends ProbDensFunction> distributionClass) {
			this.probDensFunctionClass = distributionClass;
			ProbDensFunctionType annotation = distributionClass.getAnnotation(ProbDensFunctionType.class);				
			probDensFunctionName = annotation.name();
			probDensFunctionUnivariateName = annotation.univariateName();
			if (probDensFunctionUnivariateName.equals("default")){
				probDensFunctionUnivariateName=probDensFunctionName;
			}
			probDensFunctionParametersName = annotation.parameters();
			setProbDensFunctionParametrizationName(probDensFunctionParametersName[0]);
        	for (int i =1; i< probDensFunctionParametersName.length; i++){ 
        		setProbDensFunctionParametrizationName(getProbDensFunctionParametrizationName() + ", " + probDensFunctionParametersName[i]);
        	}	
			translateDistributionIntoPseudoVariable(probDensFunctionParametersName);
		}

		/**
		 * @return the probDensFunctionName
		 */
		public String getProbDensFunctionName() {
			return probDensFunctionName;
		}

			
		/**
		 * @param probDensFunctionName the probDensFunctionName to set
		 */
		public void setProbDensFunctionName(String probDensFunctionName) {
			this.probDensFunctionName = probDensFunctionName;
		}

		/**
		 * @return the probDensUnivariateParameters
		 */
		public String[] getProbDensFunctionParametersName() {
			return probDensFunctionParametersName;
		}
		
		/**
		 * @return the probDensUnivariateName
		 */
		public String getProbDensFunctionUnivariateName() {
			return probDensFunctionUnivariateName;
		}

		/**
		 * @param probDensUnivariateName the probDensUnivariateName to set
		 */
		public void setProbDensFunctionUnivariateName(String probDensUnivariateName) {
			this.probDensFunctionUnivariateName = probDensUnivariateName;
		}
		
		
		
		
		/**
		 * @param probDensUnivariateParameters the probDensUnivariateParameters to set
		 */
		public void setProbDensFunctionParametersName(String[] probDensFunctionParametersName) {
			this.probDensFunctionParametersName = probDensFunctionParametersName;
		}

		

		public String getProbDensFunctionParametrizationName() {
			return probDensFunctionParametrizationName;
		}

		public void setProbDensFunctionParametrizationName(String probDensFunctionParametrizationName) {
			this.probDensFunctionParametrizationName = probDensFunctionParametrizationName;
		}

		/**
	     * 
	     */
	    protected void translateDistributionIntoPseudoVariable(String[] probDensFunctionParametersName){
	    	pseudoVariableDistribution = new Variable(PSEUDO_VARIABLE, probDensFunctionParametersName);
	    }
	      
	    /**
		 * @return the pseudoVariableDistribution
		 */
		public Variable getPseudoVariableDistribution() {
			return pseudoVariableDistribution;
		}

		/**
		 * @param pseudoVariableDistribution the pseudoVariableDistribution to set
		 */
		public void setPseudoVariableDistribution(Variable pseudoVariableDistribution) {
			this.pseudoVariableDistribution = pseudoVariableDistribution;
		}

		
	    public TablePotential getDistributionTable() {
			return distributionTable;
		}

		public void setDistributionTable(TablePotential tableDistr) {
			this.distributionTable = tableDistr;
		}
		
		public void setDistributionTable(List<Variable> potentialVariables, PotentialRole role) {
			List<Variable> vDistributionTable= new ArrayList<Variable>(potentialVariables);
			if (variables.get(0).getVariableType()==VariableType.NUMERIC){
				vDistributionTable.remove(0);
			}       	          
			vDistributionTable.add(0,pseudoVariableDistribution);
			setDistributionTable(new TablePotential(vDistributionTable, role));
		}
		
		public void checkDistributionValues(double[] values) throws IllegalArgumentException{
			 ProbDensFunction p= getProbDensFunctionManager().newInstance(probDensFunctionName, values);
			 try {
				 p.verifyParameters(values);
			 } catch(IllegalArgumentException e){
				 throw(e);
			 }
			 
			 
		}
		
		
		/**
	     * Now it is always true
	     * Returns if an instance of a certain Potential type makes sense given the
	     * variables and the potential role.
	     * 
	     * @param node
	     *            . <code>Node</code>
	     * @param variables
	     *            . <code>List</code> of <code>Variable</code>.
	     * @param role
	     *            . <code>PotentialRole</code>.
	     */
	    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
	        boolean suitable = true;
	        if (!(node.getVariable().getVariableType()==VariableType.NUMERIC)){
	        	suitable=false;
	        }
	       
	        int i = 1;
	        while (suitable && i < variables.size()) {
	            suitable &= variables.get(i).getVariableType() == VariableType.FINITE_STATES
	                    || variables.get(i).getVariableType() == VariableType.DISCRETIZED;
	            ++i;
	        }
	        return suitable;
	    }
	    


		@Override
	    public  List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException, WrongCriterionException{
	    	return null;
	    }
	    
	    
	    @Override
	    public  UnivariateDistrPotential project(EvidenceCase evidenceCase) throws WrongCriterionException, NonProjectablePotentialException {
	    	return null;
	    }    
	    
	    @Override
	    public  List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException{
	    	return null;
	    }
	    
	    @Override
	    public  Potential copy(){
	    	return new UnivariateDistrPotential(this);
	    }
	    
	    @Override
	    public boolean isUncertain() {
	        return false;
	    }

	    /**
	     * UNCLEAR --> Makes sense??
	     */
	    
	    @Override
	    public void scalePotential(double scale) {
	        this.getDistributionTable().scalePotential(scale);
	    }


	    public Variable getChildVariable() {
	        return this.getVariable(0);
	    }

	    public void setChildVariable(Variable childVariable) {
	        this.getVariables().set(0, childVariable);
	    }
	    
	    public void setUncertainValues(UncertainValue[] uncertainValues) {
	        getDistributionTable().setUncertainValues(uncertainValues);
	    }

	    public UncertainValue[] getUncertainValues () {
	        return getDistributionTable().getUncertainValues();
	    }

	    public void setValues (double[] values) {
	        this.getDistributionTable().values = values;
	    }

	    public double[] getValues() {
	        return getDistributionTable().getValues();
	    }

	   

	    @Override
	    public void setComment(String comment) {
	        super.setComment(comment);
	        this.getDistributionTable().setComment(comment);
	    }

	  
	    @Override
	    public String toString() {
	        StringBuilder buffer = new StringBuilder ();
	        buffer.append(variables.get(0).getName());
	        if (variables.size() == 1 ) {
	            buffer.append (" = ");
	        } else if (variables.size() > 1) {
	            buffer.append (" | ");
	            // Print variables
	            for (int i = 1; i < variables.size() - 1; i++) {
	                buffer.append(variables.get (i));
	                buffer.append(", ");
	            }
	            buffer.append (variables.get (variables.size() - 1));
	            buffer.append (" = ");
	        }
            buffer.append("UnivariteName" + probDensFunctionUnivariateName + " " + "Parametrization" + probDensFunctionParametrizationName + " ");
            
	        if (getDistributionTable().values.length == 1) {
	            buffer.append(getDistributionTable().values[0]);
	        } else if (getDistributionTable().values.length > 1) {
	            buffer.append("{");
	            for (int i = 0; i < getDistributionTable().values.length; i++) {
	                buffer.append(getDistributionTable().values[i]);
	                if (i != getDistributionTable().values.length - 1) {
	                    buffer.append(",");
	                }
	            }
	            buffer.append("}");
	        }
	        return buffer.toString ();
	    }
	
}
