package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Collection;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.UncertainValue;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.Util;

/** A <code>TablePotential</code> is a type of relation with a list of
  *   probabilistic nodes. All variables will be discrete in this class.<p> 
  * Attributes <code>dimensions</code> and <code>offsets</code> only make sense
  *   when the number of variables is greater than 0. Please be careful to check
  *   it when necessary.
  * @author marias
  * @author fjdiez
  * @version 1.0
  * @since OpenMarkov 1.0 */
@SuppressWarnings({ "unchecked" })
public class TablePotential extends FSPotential implements Comparable {

    // Attributes
    /** Dimensions (number of states) of the variables. */
    protected int[] dimensions;

    /** Offsets of the variables in the table that represents this potential. */
    protected int[] offsets;

    /** Table storing the numerical values of the potential. This attribute is
     * public for efficiency and volatile for efficiency in concurrent 
     * operations. */
    public volatile double[] values;
    
    
    /** Table storing the values of the potential for the sensitivity analysis.
     * This attribute is public for efficiency and volatile for efficiency in 
     * concurrent operations. */
    public volatile UncertainValue[] uncertainValues;
    
    public UncertainValue[] getUncertainTable() {
		return uncertainValues;
	}


	/** Indicates the first configuration. In a new potential it is 0. 
     * In a projected potential it may be different from 0. */
	private int initialPosition = 0;
	
	/** Indicates the number of configurations in this potentials. Note that
	 * this number can be less than <code>table.length</code> when the 
	 * <code>TablePotential</code> is a projection. */
	protected int tableSize;
	
	/** In projected potentials, collection of variables of the original 
	 *  potential. Original variables are used to calculate accumulated offsets.
	 */
	protected ArrayList<Variable> originalVariables;

    /** This object has a function that returns the available memory. 
     * Used in the constructor before creating the <code>table</code> */
    protected static Runtime runtime = Runtime.getRuntime();

    // Constructors
	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 *   used to build the <code>TablePotential</code>.
	 * @param role. <code>PotentialRole</code>
	 * @throws <code>NotEnoughMemoryException</code> */
    public TablePotential(ArrayList<Variable> variables, PotentialRole role) 
    		throws NotEnoughMemoryException {
    	super(variables, role);
    	this.originalVariables = this.variables;
		if (numVariables != 0) {
			dimensions = TablePotential.calculateDimensions(variables);
			offsets = TablePotential.calculateOffsets(dimensions);
		    tableSize = dimensions[numVariables - 1] * 
		        offsets[numVariables - 1];
			long freeMemory = runtime.freeMemory();
			if (freeMemory < (tableSize * (Double.SIZE / 8))) {
				throw new NotEnoughMemoryException("There are only " + 
					Util.printInteger(freeMemory) + " bytes free. "
					+ "Not enough memory to allocate a table with " + 
					Util.printInteger((tableSize * (Double.SIZE / 8)))	+ 
					" bytes in TablePotential constructor." 
					+ " Number of variables: " + variables.size());
			}
		    values = new double[tableSize];
		    setUniform(); // Initializes the table as an uniform potential
		} else {// In this case the potential is a constant
			tableSize = 1;
			values = new double[tableSize];
			offsets = new int[0];
		}
		type = PotentialType.TABLE;
    }
    
    /** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @param role. <code>PotentialRole</code>
     * @param table. <code>double[]</code>
     * @argCondition All variables must be discrete. */
    public TablePotential(ArrayList<Variable> variables, PotentialRole role, 
    		double[] table) {
        super(variables, role);
    	this.originalVariables = this.variables;
        this.values = table;
		if (numVariables != 0) {
			dimensions = TablePotential.calculateDimensions(variables);
			offsets = TablePotential.calculateOffsets(dimensions);
			computeTableSize();
		}
		else{
			dimensions = new int[0];
			offsets = new int[0];
			tableSize = 1;
		}
		type = PotentialType.TABLE;		
    }
    
    /** Internal constructor used to create a projected potential. 
     * @param variables. <code>ArrayList</code> of <code>Variable</code>
     * @param role. <code>PotentialRole</code>
     * @param table. <code>double[]</code>
     * @param initialPosition First position in <code>table</code>
     *  (used in projected potentials).
     * @param offsets of variables. <code>int[]</code>
     * @param dimensions. Number of states of each variable. <code>int[]</code>
     */
    private TablePotential(ArrayList<Variable> variables, PotentialRole role,
    		double[] table,	int initialPosition, 
    		int[] offsets, int[] dimensions) {
    	super(variables, role);
    	this.originalVariables = this.variables;
    	this.values = table;
    	this.initialPosition = initialPosition;
    	this.offsets = offsets;
    	this.dimensions = dimensions;
    	computeTableSize();
    	type = PotentialType.TABLE;
    }
    
    // Methods
    /** @param evidenceCase <code>EvidenceCase</code>
     * @return An <code>ArrayList</code> of <code>FSPotential</code>s 
     *  containing only one element, which is a <code>ProjectedPotential</code>
     * @throws NotEnoughMemoryException 
     * @throws WrongCriterionException 
     * @throws NoFindingException */
    public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase,
    		InferenceOptions inferenceOptions) 
    		throws NotEnoughMemoryException, WrongCriterionException {
    	// returned value
    	boolean hasUncertainTable=(uncertainValues!=null);
    	
    	ArrayList<TablePotential> projectedPotentials = 
    		new ArrayList<TablePotential>(1); 

    	ArrayList<Variable> unobservedVariables = 
    		(ArrayList<Variable>)variables.clone();
    	if (evidenceCase != null) {
    		unobservedVariables.removeAll(evidenceCase.getVariables());
    	}

    	int numUnobservedVariables = unobservedVariables.size();

    	TablePotential projectedPotential;
    	if (numVariables == numUnobservedVariables) { // No projection.
    		projectedPotential = this; 
    	} else {// Common part in constant potential and not constant potentials
    		projectedPotential = 
    			new TablePotential(unobservedVariables, role);
    		
    		int length = projectedPotential.values.length;
    		if (hasUncertainTable){
    			projectedPotential.setUncertainTable(new UncertainValue[length]);
			}
    		
    		// position (in this potential) of the first value 
    		// of the projected potential
    		int firstPosition = 0;
    		// auxiliary for the for loop
    		int state;
    		// iterate over the variables of this potential
    		for (int i = 0; i < variables.size(); i++) {
    			Variable variable = variables.get(i);
    			if ((evidenceCase != null) && evidenceCase.contains(variable)) {
    				state = evidenceCase.getState(variable);
    				firstPosition += state * offsets[i];
    			}
    		}
    		if (numUnobservedVariables == 0) {// Projection = constant potential
    			projectedPotential.values[0] = values[firstPosition];
    			if (hasUncertainTable){
    				projectedPotential.uncertainValues[0]=uncertainValues[firstPosition];
    			}
    		} else { // Create projected potential
    			// Go trough this potential using accumulatedOffests
    			int[] accumulatedOffsets = 
    				projectedPotential.getAccumulatedOffsets(variables);
    			int numVariablesProjected = 
    				projectedPotential.getNumVariables();
    			int[] projectedCoordinate = new int[numVariablesProjected];
    			int[] projectedDimensions = new int[numVariablesProjected];
    			for (int i = 0; i < numVariablesProjected; i++) {
    				projectedDimensions[i] = 
    					unobservedVariables.get(i).getNumStates();
    			}
    			
    			// Copy configurations using the accumulated offsets algorithm
				for (int projectedPosition = 0; 
    					projectedPosition < length - 1; 
    					projectedPosition++) {
    				projectedPotential.values[projectedPosition] = 
    					values[firstPosition];
    				if (hasUncertainTable){
        				projectedPotential.uncertainValues[projectedPosition] = 
        					uncertainValues[firstPosition];
        			}
    				
    				// find the next configuration and the index of the
    				// increased variable
    				int increasedVariable = 0;
    				for (int j = 0; j < projectedCoordinate.length; j++) {
    					projectedCoordinate[j]++;
    					if (projectedCoordinate[j] < projectedDimensions[j]) {
    						increasedVariable = j;
    						break;
    					}
    					projectedCoordinate[j] = 0;
    				}
    		
    				// update the positions of the potentials we are multiplying
   					firstPosition +=
   						accumulatedOffsets[increasedVariable];    				
    			}
    			int lastPositionProjected = length - 1;
    			projectedPotential.values[lastPositionProjected] = 
    				values[firstPosition];
    			if (hasUncertainTable){
    			projectedPotential.uncertainValues[lastPositionProjected] = 
    				uncertainValues[firstPosition];
    			}
    		}
    		
    		// Common final part for constant and not constant potentials
			projectedPotential.setUtilityVariable(this.utilityVariable);
			projectedPotential.setUncertainTableToNullIfNullValues();
    	}
    	
    	// discounts utilities
    	if (role == PotentialRole.UTILITY && inferenceOptions != null && 
    			inferenceOptions.discountRate != 1.0 && 
    			utilityVariable.isTemporal()) {
    		int timeSlice = utilityVariable.getTimeSlice();
    		double discount = 
    			Math.pow(inferenceOptions.discountRate, timeSlice);

    		for (int i = 0; i < projectedPotential.values.length; i++) {
    			projectedPotential.values[i] *= discount;
    		}
    	}
    	
    	// Cylindrical extension for utility potentials in the case of 
    	// multicriteria decision making
    	if (role == PotentialRole.UTILITY && inferenceOptions != null) {
    		Variable decisionCriteria = inferenceOptions.decisionCriteria;
    		ProbNet probNet = inferenceOptions.probNet;
   			String criterion = probNet.getProbNode(utilityVariable).getPurpose();
        	if (role == PotentialRole.UTILITY && decisionCriteria != null) {
        		ArrayList<Potential> potentials = new ArrayList<Potential>(2);
        		potentials.add(projectedPotential);
        		try {
    				potentials.add(
    						decisionCriteria.deltaTablePotential(criterion));
    			} catch (InvalidStateException e) {
    				throw new WrongCriterionException(utilityVariable, criterion,
    						decisionCriteria);
    			}
    		    // System.out.println("vamos a multiplicar");
        		projectedPotential = 
        			DiscretePotentialOperations.multiply(potentials);}
    	}

	    /*for (int i = 0; i < projectedPotential.values.length; i++) {
	    	System.out.print(projectedPotential.values[i] + ", ");
	    }
	    System.out.println("hemos multiplicado");*/

    	if (role == PotentialRole.UTILITY) {
    		projectedPotential.setUtilityVariable(utilityVariable);
    	}
		projectedPotentials.add(projectedPotential);
    	return projectedPotentials;
    }

    
	private void setUncertainTableToNullIfNullValues() {
		boolean allNull;
		if (uncertainValues != null){
			allNull = true;
			for (int i=0;i<uncertainValues.length&&allNull;i++){
				allNull = (uncertainValues[i]==null);
			}
			if (allNull){
				uncertainValues = null;
			}
		}
		
		
	}

	/** The accumulated offset represents the increment (positive or negative)
	 * in the corresponding position of the table when a variable is incremented
	 * given an ordering of the variables of other potential.<p>
	 * <big><b>
	 * Accumulated Offsets example<p>
	 * </b></big>
	 * We have two potentials:
	 * Potential <b>Y</b> (b, d, a, c)  and  Potential <b>X</b> (a, b, c). 
	 * All variables are binary for simplicity.<p>
	 * <p>
	 * <table border="2">
	 * <caption ALIGN="top">
	 * </caption>
	 * <tr><td><b><center>Y</center></b></td> 
	 * <td><b>pos<sub>y</sub>(Y)</b>
	 * </td> <td><b><center>Y<sup>X</sup></center></b></td>  
	 * <td><b>pos<sub>X</sub>(Y<sup>X</sup>)</b></td> 
	 * <td><b>varToIncr(Y)</b></td> <td><b>accOffset</b></td></tr>
	 * <tr></tr><td>[b<sub>0</sub>,d<sub>0</sub>,a<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>0</center></td>
	 * <td>[a<sub>0</sub>,b<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>0</center></td>
	 * <td><center>0(B)</center></td><td><center>+2</center></td>
	 * <tr></tr><td>[b<sub>1</sub>,d<sub>0</sub>,a<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>1</center></td>
	 * <td>[a<sub>0</sub>,b<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>2</center></td>
	 * <td><center>1(D)</center></td><td><center>-2</center></td>
	 * <tr></tr><td>[b<sub>0</sub>,d<sub>1</sub>,a<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>2</center></td>
	 * <td>[a<sub>0</sub>,b<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>0</center></td>
	 * <td><center>0(B)</center></td><td><center>+2</center></td>
	 * <tr></tr><td>[b<sub>1</sub>,d<sub>1</sub>,a<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>3</center></td>
	 * <td>[a<sub>0</sub>,b<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>2</center></td>
	 * <td><center>2(A)</center></td><td><center>-1</center></td>
	 * <tr></tr><td>[b<sub>0</sub>,d<sub>0</sub>,a<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>4</center></td>
	 * <td>[a<sub>1</sub>,b<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>1</center></td>
	 * <td><center>0(B)</center></td><td><center>+2</center></td>
	 * <tr></tr><td>[b<sub>1</sub>,d<sub>0</sub>,a<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>5</center></td>
	 * <td>[a<sub>1</sub>,b<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>3</center></td>
	 * <td><center>1(D)</center></td><td><center>-2</center></td>
	 * <tr></tr><td>[b<sub>0</sub>,d<sub>1</sub>,a<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>6</center></td>
	 * <td>[a<sub>1</sub>,b<sub>0</sub>,c<sub>0</sub>]
	 *     </td><td><center>1</center></td>
	 * <td><center>0(B)</center></td><td><center>+2</center></td>
	 * <tr></tr><td>[b<sub>1</sub>,d<sub>1</sub>,a<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>7</center></td>
	 * <td>[a<sub>1</sub>,b<sub>1</sub>,c<sub>0</sub>]
	 *     </td><td><center>3</center></td>
	 * <td><center>3(C)</center></td><td><center>+1</center></td>
	 * <tr></tr><td><center>...</center></td><td><center>...</center></td>
	 * <td><center>...</center></td><td><center>...</center></td>
	 * <td><center>...</center></td><td><center>...</center></td>
	 * </table>
	 * <p>
	 * The order is imposed by the variables of <b>this</b> potential 
	 * (<b>Y</b>)<p>
	 * @param otherVariables <code>ArrayList</code> of <code>Variable</code>s
	 *  of another potential (in this example: <b>Y<sup>X</sup></b> = [a, b, c])
	 * @return The accumulated offsets in an array of integers. In this example 
	 *   Accumulated offsets returns: [+2,-2,-1,+1]. Size = this 
	 *   <code>TablePotential</code> number of variables. */
	public int[] getAccumulatedOffsets(ArrayList<Variable> otherVariables) {
		int otherSize = otherVariables.size();
		int thisSize = variables.size();
		int[] accOffsetXY = new int[thisSize];
		if (otherSize == 0) {
			return accOffsetXY; // Initialized to 0
		}
		int[] ordering = new int[thisSize];
		for (int i = 0; i < ordering.length; i++) {
			ordering[i] = otherVariables.indexOf(variables.get(i));
		}
		
		// offsets of otherVariables
		int[] offsetX = new int[otherSize];
		offsetX[0] = 1;
		for (int i = 1; i < offsetX.length; i++) {
			offsetX[i] = offsetX[i-1] * otherVariables.get(i-1).getNumStates(); 
		}
		int[] offsetXY = new int[thisSize]; 
		int ordering_0 = ordering[0];
		if (ordering_0 == -1) {
			offsetXY[0] = 0;
		} else {
			offsetXY[0] = offsetX[ordering_0];
		}
		accOffsetXY[0] = offsetXY[0];
		
		int ordering_j;
		for (int j = 1; j < accOffsetXY.length; j++) {
			ordering_j = ordering[j];
			if (ordering_j == -1) {
				offsetXY[j] = 0;
			} else {
				offsetXY[j] = offsetX[ordering_j];
			}
			int numStatesYj_1 = ((Variable)variables.get(j-1)).getNumStates();
			accOffsetXY[j] = accOffsetXY[j-1] + offsetXY[j] - 
			    ( numStatesYj_1 * offsetXY[j - 1] );
		}
		
		return accOffsetXY;
	}
	
	/** Get accumulated offsets of a projected potential.
	 * @param otherVariables. Actual set of variables in a projected potential.
	 *  <code>ArrayList</code> of <code>Variable</code>
	 * @param originalVariables. Complete set of variables in a projected
	 *  potential. <code>ArrayList</code> of <code>Variable</code>
	 * @return The accumulated offsets in an array of integers. 
	 * @argCondigion otherVariables is contained in originalVariables.
	 * @argCondigion otherVariables and originalVariables have the same order.*/
	public int[] getProjectedAccumulatedOffsets(
			ArrayList<Variable> otherVariables, 
			ArrayList<Variable> originalVariables) {
		if (otherVariables == originalVariables) { // Not projected potential
			return getAccumulatedOffsets(otherVariables);
		}
		int[] originalAccOffsets = getAccumulatedOffsets(originalVariables);
		int[] accOffsets = new int[otherVariables.size()];
		int j = 0;
		for (int i = 0; i < originalVariables.size(); i++) {
			Variable variable = originalVariables.get(i);
			if (otherVariables.contains(variable)) {
				accOffsets[j++] = originalAccOffsets[i];
			}
		}
		return accOffsets;
	}

	/** A configuration is a set of integers that represents a position in the
	 * table.<p>
	 * <strong>Example</strong>: A potential <strong>T</strong> with
	 * two binary variables: <strong>a</strong> and <strong>b</strong> has
	 * this possible configurations and position in the table:<p>
	 * <TABLE BORDER=1 ALIGN=CENTER>
	 * <tr><td><strong>a  b position</strong></td></tr>
	 * <tr><td>0  0    0</td></tr>
	 * <tr><td>1  0    1</td></tr>
	 * <tr><td>0  1    2</td></tr>
	 * <tr><td>1  1    3</td></tr>
	 * </TABLE>
	 * <p><p>
	 * @return The position in the table of the value 
     * corresponding to the given coordinates of the variables.<p>
     * In the above example <code>T.getPosition([0,1])</code> will return: 
     * <strong>2</strong>
     * @argCondition coordinates.length = numVariables
     * @argCondition coordinates[i] >= 0 and coordinates[i] < dimensions[i]. */
    public int getPosition(int[] coordinates) {
        int position = 0;
        for (int i = 0; i < numVariables; i++) {
            position += offsets[i] * coordinates[i];
        }
        return position;
    }
    
    
    /**
     * This method is similar to getPosition(int []), but the input argument is a configuration of
     * variables which are not necessarily in the same order that the variables in
     * the potential
     * @param potential
     * @param configuration
     */
    private int getPosition(EvidenceCase configuration) {

		int[] coordinates;
		int sizeCoordinates;
		int pos;
		boolean isChanceVariable;
		
		int sizeEvi = configuration.getFindings().size();
		
		isChanceVariable = !(this.isUtility());
		
		sizeCoordinates = sizeEvi + (isChanceVariable?1:0);
		coordinates = new int[sizeCoordinates];
		
		ArrayList<Variable> varsTable = this.getVariables();
		
		int startLoop;
		if (isChanceVariable){
			coordinates[0]=0;
			startLoop = 1;
		}
		else{
			startLoop = 0;
		}
		for (int i=startLoop;i<sizeCoordinates;i++){
			coordinates[i]=configuration.getFinding(varsTable.get(i)).getStateIndex();
		}
		
		pos = getPosition(coordinates);
		return pos;
	}
    
    
    
    /**
     * It returns the first position in the table of the consecutive cells where all the values corresponding
     * to a certain configuration are stored. It assumes that configuration is a complete instantiation of the parents
     * of the variable associated to the table.
     * @param configuration
     * @return
     */
    public int getBasePosition(EvidenceCase configuration) {
		int[] coordinates;
		int sizeCoordinates;
		int pos;
		boolean isChanceVariable;
		isChanceVariable = !(this.isUtility());
		int sizeEvi = configuration.getFindings().size();
		sizeCoordinates = sizeEvi + (isChanceVariable?1:0);
		coordinates = new int[sizeCoordinates];
		ArrayList<Variable> varsTable = this.getVariables();
		int startLoop;
		if (isChanceVariable){
			coordinates[0]=0;
			startLoop = 1;
		}
		else{
			startLoop = 0;
		}
		for (int i=startLoop;i<sizeCoordinates;i++){
			coordinates[i]=configuration.getFinding(varsTable.get(i)).getStateIndex();
		}
		pos = this.getPosition(coordinates);
		return pos;
    }
    
    
    /** @param position in the table. <code>int</code> 
     * @return The configuration corresponding to <code>position</code>
     *  <code>double</code> */
    public int[] getConfiguration(int position) {
    	int[] coordinate = new int[offsets.length];
    	for (int i = offsets.length-1; i >= 0; i--) {
    		coordinate[i] = position / offsets[i];
    		position -= coordinate[i] * offsets[i];
    	}
    	return coordinate;
    }
    
    /** Given a set of variables and a set of corresponding states indices,
     * gets the corresponding value in the table.
     * @argCondition All the variables in this potentials are included into the
     * received variables.
     * @param variables. <code>ArrayList</code> of <code>Variable</code>
     * @param stateIndices. <code>int[]</code>
     * @return <code>double</code> */
    public double getValue(ArrayList<Variable> variables, int[] statesIndices) {
    	int position = 0;
    	for (int i = 0; i < variables.size(); i++) {
    		Variable variable = variables.get(i);
    		int indexVariable = this.variables.indexOf(variable);
    		if (indexVariable != -1) {
    			position += offsets[indexVariable] * statesIndices[i];
    		}
    	}
    	return values[position];
    }

	/** @return <code>int[]</code>: The offsets of the variables in the table
	 * of values.
	 * @consultation */
	public int[] getOffsets() {
		return offsets;
	}

	/** @return <code>double[]</code>: Table containing the values of the 
	 * potential.
	 * @consultation */
	public double[] getValues() {
		return values;
	}
	
	/**
	 * The dimensions of the new table have to be same that the current table
	 * @return <code>double[]</code>: Table containing the values of the 
	 * potential.
	 * 
	 * @consultation */
	public void setValues(double [] table) {
		this.values = table;
	}

    /** @consultation
	 * @return dimensions of the variables in an array of <code>int[]</code>. */
    public int[] getDimensions() {
        return dimensions;
    }

	/** This method is <code>static</code> because sometimes can be used
	 *    outside of a <code>TablePotential</code>.
	 * @param dimensions of variables. Array of <code>int[]</code>.
	 * @return array of <code>int[]</code> with the offset of each variable. */
	public static int[] calculateOffsets(int[] dimensions) {
		int[] offsets;
		int numVariables = dimensions.length;
		offsets = new int[numVariables];
		offsets[0] = 1;
		for (int i = 1; i < numVariables; i++) {
			offsets[i] = dimensions[i - 1] * offsets[i - 1];
		}
		return offsets;
	}

	/** @return <code>initialPosition int</code>.
	 * @consultation */
	public int getInitialPosition() {
		return initialPosition;
	}

	/** Compares two <code>TablePotential</code>s using <code>tableSize</code>
	 *   as a criterion.
	 * @param tablePotential <code>Object</code>. 
	 * @return <code>int</code>:<p><0 if <code>this</code> table size is minor 
	 *   than the received potential<p>=0 if tables size is equal<p>>0 if 
	 *   <code>this</code> table size is greater than the table size of received
	 *   potential. */
	public int compareTo(Object tablePotential) {
		TablePotential other = (TablePotential)tablePotential;
		return this.tableSize - other.tableSize;
	}
    
	/**
	 * @param configuration
	 * @return true if and only if the potential contains uncertainty values
	 * for a certain configuration
	 */
	public boolean hasUncertainty(EvidenceCase configuration){
		boolean hasUncertainty;
		if (uncertainValues==null){
			hasUncertainty = false;
		}
		else{
			int positionConfiguration;
			positionConfiguration = getPosition(configuration);
			hasUncertainty = uncertainValues[positionConfiguration]!=null;
		}
		return hasUncertainty;
	}
	
	
	/** @return <code>ArrayList</code> of <code>Variable</code>s. 
	 * @consultation */
	public ArrayList<Variable> getVariables() {
		if (variables != null) {
			return (ArrayList<Variable>)variables.clone();
		} else {
			return variables;
		}
	}
	
	/** @return tableSize <code>int</code> */
	public int getTableSize() {
		return tableSize;
	}
	
	/** @return originalVariables. <code>ArrayList</code> of 
	 *  <code>Variable</code> */
	public ArrayList<Variable> getOriginalVariables() {
		return originalVariables;
	}
	
	/** @param originalVariables. <code>ArrayList</code> of 
	 *  <code>Variable</code> */
	public void setOriginalVariables(ArrayList<Variable> originalVariables) {
		this.originalVariables = originalVariables; 
	}

	//TODO revisar para que no use tableProject(...)
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase)
	throws IncompatibleEvidenceException, NotEnoughMemoryException, WrongCriterionException {
		Collection<Finding> inducedFindings =  new ArrayList<Finding>();
		if (role == PotentialRole.CONDITIONAL_PROBABILITY ||
				role == PotentialRole.POLICY) { 
			// Iterates over the list of parents. If some parent is not in the 
			// evidence case, it is not possible to induce a new Finding
			for (int i = 1; i < variables.size(); i++) {
				if (!evidenceCase.contains(variables.get(i))) {
					// returnS the empty list
					return inducedFindings;
				}
			}
			// Checks if the projected potentials are deterministic
			TablePotential projectedPotential = 
				tableProject(evidenceCase, null).get(0);
			if ((projectedPotential.getNumVariables() == 1) && 
					(projectedPotential.getPotentialType() == 
						PotentialType.TABLE)) {
				double[] table = ((TablePotential)projectedPotential).values;
				int zeros = 0;
				int position = 0;
				for (int i = 0; i < table.length; i++) {
					if (table[i] == 0.0) {
						zeros++;
					} else {
						position = i;
					}
				}
				if (zeros == (table.length - 1)) {// new finding
					inducedFindings.add(
							new Finding(projectedPotential.getVariable(0), 
									position));
				}

			}
			return inducedFindings;
		} else {
			// returns the empty list
			return inducedFindings;
		}
	}

	
	/** Initialize the table as a uniform potential. */
	public void setUniform() {
		if ((variables != null) && (variables.size() > 0) && 
				allVariablesBelongsToType(VariableType.FINITE_STATES) && 
				( (role == PotentialRole.CONDITIONAL_PROBABILITY) ||
				  (role == PotentialRole.POLICY) ||
				  (role == PotentialRole.JOIN_PROBABILITY) ||
				  (role == PotentialRole.UTILITY)) ) {
			Double value = 0.0;
			switch(role) {
			case CONDITIONAL_PROBABILITY:
				value = 1.0 / new Double(variables.get(0).getNumStates());
				break;
			case POLICY:
			case JOIN_PROBABILITY:
				value = 1.0;
				for (Variable variable : variables) {
					value *= variable.getNumStates();
				}
				value = 1 / value;
				break;
			} // When role = UTILITY -> value = 0.0 (default)
			for (int i = 0; i < values.length; i++) {
				values[i] = value;
			}
		}
	}

	/** Overrides <code>toString</code> method. Mainly for test purposes */
	public String toString() {
		// writes variables names
		StringBuffer buffer = new StringBuffer(super.toString());
		// Print configurations
		int valuesPosition = 0;
		if (buffer.length() < maxLengthString) {
			if (variables.size() > 0) {
				buffer.append(" = {");
			} else {
				if (role == PotentialRole.UTILITY) {
					buffer.append(" = ");
				} else {
					buffer.append(" ");
				}
			}
		}
		while ((buffer.length() < maxLengthString) && 
				(valuesPosition < values.length)) {
			buffer.append(values[valuesPosition++]);
			if ((valuesPosition < values.length) &&
					(buffer.length() < (maxLengthString - 2))) {
			    buffer.append(", ");
			}
		}
		if (values.length != 1) {
			if (valuesPosition == values.length && variables.size() > 0) {
				buffer.append("}");
			} else {
				buffer.append("...}");
			}
		}

		return buffer.toString();
	}
	
	public String treeADDString() {
		if (role == PotentialRole.CONDITIONAL_PROBABILITY && numVariables == 1) 
		{
			Variable firstVariable = variables.get(0);
			for (int i = 0; i < firstVariable.getNumStates(); i++) {
				if (values[i] == 1) {
					return firstVariable.getName() + " = " + 
					firstVariable.getStateName(i);
				}
			}
		}
		return toString();
	}


	/** Calculates <code>tableSize</code> = product of dimensions of variables.
	 *  In projected potentials <code>tableSize</code> can be distinct that 
	 *  <code>table.length</code>. */
	private void computeTableSize() {
		tableSize = 1;
		for (Variable variable : (ArrayList<Variable>)((Object)variables)) {
			tableSize *= variable.getNumStates();
		}
	}

	/**
	 * @param uncertainTable
	 */
	public void setUncertainTable(UncertainValue[] uncertainTable) {
		this.uncertainValues = uncertainTable;
	}
	
	
	/**
	 * @param uncertainTable
	 * @return true if the uncertain values are correct 
	 */
	public static boolean checkUncertainTable(ArrayList<UncertainValue> uncertainTable){
		
		return true;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeDifference) 
	throws ProbNodeNotFoundException, NotEnoughMemoryException {
		ArrayList<Variable> shiftedVariables = 
			getShiftedVariables(probNet, timeDifference);
		TablePotential shiftedPotential = 
			new TablePotential(shiftedVariables, role);
		if (role == PotentialRole.UTILITY) {
			shiftedPotential.setUtilityVariable(
				probNet.getShiftedVariable(utilityVariable, timeDifference));
		}
		// Copy the table because it can be modified later in one potential and
		// not in the other, i.e. applying a discount in the shifted potential
		for (int i = 0; i < values.length; i++) {
			shiftedPotential.values[i] = values[i];
		}
		return shiftedPotential;
	}
	
	/**
	 * Generates a sampled potential
	 */
	public Potential sample(Variable simulationIndexVariable)
	throws NotEnoughMemoryException {
    	if (simulationIndexVariable != null) {
    		SamplePotentialTable samplePotentialTable =
    			new SamplePotentialTable(this, simulationIndexVariable);
    		return samplePotentialTable.getSampledTable();
    	}
    	return this;
	}
	
}