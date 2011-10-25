package org.openmarkov.core.model.network.potential.operation;

import java.awt.Choice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.openmarkov.core.exception.IllegalArgumentTypeException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/** This class defines a set of common operations over discrete potentials 
 * (<code>TablePotential</code>s) and discrete variables
 * (<code>Variable</code>s).
 * The method are invoked from <code>PotentialOperations</code> after checking
 * that the parameters are discrete.
 * @author marias */
public final class DiscretePotentialOperations {
	
	/** Round error used to compare two numbers. If they differ in less than 
	 * <code>maxRoundErrorAllowed</code> they will be considered equals. */
	public static double maxRoundErrorAllowed = 1E-5; 

    /** @param tablePotentials <code>ArrayList</code> of extends
     * <code>Potential</code>.
     * @return A <code>TablePotential</code> as result. 
     * @throws WrongCriterionException 
     * @throws NonProjectablePotentialException */
	@SuppressWarnings("unchecked")
	public static TablePotential multiply(
			ArrayList<? extends Potential> tablePotentials)
	throws NotEnoughMemoryException {
		
		int numPotentials = tablePotentials.size();
		// Special cases: one or zero potentials
		if (numPotentials < 2) {
			if (numPotentials == 1) {
				return (TablePotential)tablePotentials.get(0);
			} else {
				return null;
			}			
		}
		
		ArrayList<TablePotential> potentials = 
			(ArrayList<TablePotential>)((Object)tablePotentials);

		// Sort the potentials according to the table size
		Collections.sort(potentials);
		
		// Gets constant factor: The product of constant potentials
		double constantFactor = getConstantFactor(potentials);
		
		// get role
		PotentialRole role = getRole(potentials);
		
		potentials = 
			AuxiliaryOperations.getProperPotentials(potentials);
		if (potentials.size() == 0) {
			TablePotential constantTablePotential = 
				new TablePotential(null, role);
			constantTablePotential.values[0] = constantFactor;
			return constantTablePotential;
		}

		// Gets the union
		TablePotential result =	new TablePotential((ArrayList<Variable>)
			((Object)AuxiliaryOperations.getUnionVariables(potentials)), role);

		int numVariables = result.getNumVariables();

		// Gets the tables of each TablePotential
		numPotentials = potentials.size();
		double[][] tables = new double[numPotentials][];
		for (int i = 0; i < numPotentials; i++) {
			tables[i] = potentials.get(i).values;
		}

		// Gets dimension
		int[] resultDimension = result.getDimensions();

		// Gets offset accumulate
		int[][]offsetAccumulate = DiscretePotentialOperations
			.getAccumulatedOffsets(potentials, result);

		// Gets coordinate
		int[] resultCoordinate;
		if (numVariables != 0) {
			resultCoordinate = new int[numVariables];
		} else {
			resultCoordinate = new int[1];
			resultCoordinate[0] = 0;
		}

		// Position in each table potential
		int[] potentialsPositions = new int[numPotentials];
		for (int i = 0; i < numPotentials; i++) {
			potentialsPositions[i] = 0;
		}

		// Multiply
		int incrementedVariable = 0;
		double mulResult;
		int[] dimension = result.getDimensions();
		int[] offset = result.getOffsets();
		int tamTable = 1; // If numVariables == 0 the potential is a constant
		if (numVariables > 0) {
		    tamTable = dimension[numVariables - 1] * offset[numVariables - 1];
		}

		for (int resultPosition=0; resultPosition<tamTable; resultPosition++) {
			mulResult = constantFactor;

           /* increment the result coordinate and
			   find out which variable is to be incremented */
			for (int iVariable=0; iVariable < resultCoordinate.length;
					iVariable++) {
				// try by incrementing the current variable (given by iVariable)
				resultCoordinate[iVariable]++;
				if (resultCoordinate[iVariable] != resultDimension[iVariable]) {
					// we have incremented the right variable
					incrementedVariable = iVariable;
					// do not increment other variables;
					break;
				}
				/* this variable could not be incremented;
				   we set it to 0 in resultCoordinate
				   (the next iteration of the for-loop will increment
				   the next variable) */
				resultCoordinate[iVariable] = 0;
			}

			// multiply
			for (int iPotential=0; iPotential < numPotentials;
			    	iPotential++) {
				// multiply the numbers
				mulResult = mulResult *
				    tables[iPotential][potentialsPositions[iPotential]];
				// update the current position in each potential table
				potentialsPositions[iPotential] +=
					offsetAccumulate[iPotential][incrementedVariable];
			}
			result.values[resultPosition] = mulResult;
		}
		return result;
	}

    /** @param tablePotentials <code>ArrayList</code> of <code>extends 
     * Potential</code>.
     * @throws <code>NotEnoughMemoryException</code>. */
    @SuppressWarnings("unchecked")
    public static TablePotential sum(
            ArrayList<? extends Potential> tablePotentials)
            throws NotEnoughMemoryException {
        if (tablePotentials.size() == 1) {
        	return (TablePotential)tablePotentials.get(0);
        }
        
        ArrayList<TablePotential> potentials = 
            (ArrayList<TablePotential>)((Object)tablePotentials);
        
        int numPotentials = potentials.size();
        PotentialRole role = getRole(tablePotentials);
        
        // Gets the union
        TablePotential result = new TablePotential((ArrayList<Variable>)
            ((Object)AuxiliaryOperations.getUnionVariables(potentials)), role);

        int numVariables = result.getNumVariables();

        // Gets the tables of each TablePotential
        double[][] tables = new double[numPotentials][];
        for (int i = 0; i < numPotentials; i++) {
            tables[i] = potentials.get(i).values;
        }

        // Gets dimension
        int[] resultDimension = result.getDimensions();

        // Gets offset accumulate
        int[][]offsetAccumulate = DiscretePotentialOperations
            .getAccumulatedOffsets(potentials, result);

        // Gets coordinate
        int[] resultCoordinate;
        if (numVariables != 0) {
            resultCoordinate = new int[numVariables];
        } else {
            resultCoordinate = new int[1];
            resultCoordinate[0] = 0;
        }

        // Position in each table potential
        int[] potentialsPositions = new int[numPotentials];
        for (int i = 0; i < numPotentials; i++) {
            potentialsPositions[i] = 0;
        }

        // Add
        int incrementedVariable = 0;
        int[] dimension = result.getDimensions();
        int[] offset = result.getOffsets();
        int tamTable = 1; // If numVariables == 0 the potential is a constant
        if (numVariables > 0) {
            tamTable = dimension[numVariables - 1] * offset[numVariables - 1];
        }

        double addResult;
        for (int resultPosition=0; resultPosition<tamTable; resultPosition++) {
           /* increment the result coordinate and
               find out which variable is to be incremented */
            for (int iVariable=0; iVariable < resultCoordinate.length;
                    iVariable++) {
                // try by incrementing the current variable (given by iVariable)
                resultCoordinate[iVariable]++;
                if (resultCoordinate[iVariable] != resultDimension[iVariable]) {
                    // we have incremented the right variable
                    incrementedVariable = iVariable;
                    // do not increment other variables;
                    break;
                }
                /* this variable could not be incremented;
                   we set it to 0 in resultCoordinate
                   (the next iteration of the for-loop will increment
                   the next variable) */
                resultCoordinate[iVariable] = 0;
            }
            addResult = 0;

            // multiply
            for (int iPotential=0; iPotential < numPotentials;
                    iPotential++) {
                // multiply the numbers
                addResult = addResult +
                    tables[iPotential][potentialsPositions[iPotential]];
                // update the current position in each potential table
                potentialsPositions[iPotential] +=
                    offsetAccumulate[iPotential][incrementedVariable];
            }
            result.values[resultPosition] = addResult;
        }
        return result;
    }

	private static PotentialRole getRole(
			ArrayList<? extends Potential> potentials) {
		boolean atLeastOneUtility = false;
		for (Potential potential : potentials) {
			atLeastOneUtility = atLeastOneUtility || potential.isUtility();
		}
		if (atLeastOneUtility) {
			return PotentialRole.UTILITY;
		}
		boolean atLeastOneJoinProbability = false;
		for (Potential potential : potentials) {
			atLeastOneJoinProbability  = atLeastOneJoinProbability  || 
				potential.getPotentialRole() == PotentialRole.JOIN_PROBABILITY;
		}
		if (atLeastOneJoinProbability) {
			return PotentialRole.JOIN_PROBABILITY;
		}
		return PotentialRole.CONDITIONAL_PROBABILITY;
	}

	/** @param tablePotentials array to multiply
	 * @param fsVariablesToKeep The set of variables that will appear in the 
	 * resulting potential
	 * @param fsVariablesToEliminate The set of variables eliminated by 
	 * marginalization (in general, by summing out or maximizing)
	 * @argCondition variablesToKeep and variablesToEliminate are a partition of
	 * the union of the variables of the potential
	 * @return A <code>TablePotential</code> result of multiply and marginalize.
	 * @trows NotEnoughMemoryException */
    @SuppressWarnings("unchecked")
	public static TablePotential multiplyAndMarginalize(
			ArrayList<? extends Potential> tablePotentials, 
			ArrayList<Variable> variablesToKeep,
			ArrayList<Variable> variablesToEliminate) 
    		throws NotEnoughMemoryException {

    	ArrayList<TablePotential> potentials = 
    		(ArrayList<TablePotential>)((Object)tablePotentials);
    	
		TablePotential resultingPotential = new TablePotential(
				variablesToKeep, getRole(tablePotentials));
		
		// Constant potentials are those that do not depend on any variables.
		// The product of all the constant potentials is the constant factor.
		double constantFactor = 1.0;
		// Non constant potentials are proper potentials.
		ArrayList<TablePotential> properPotentials =
			new ArrayList<TablePotential>();
		for (Potential potential : potentials) {
			if (potential.getNumVariables() != 0) {
				properPotentials.add((TablePotential) potential);
			} else {
				constantFactor *= ((TablePotential)potential)
					.values[((TablePotential) potential).getInitialPosition()];
			}
		}
		
		int numProperPotentials = properPotentials.size();
		
		if (numProperPotentials == 0) {
			resultingPotential.values[0] = constantFactor;
			return resultingPotential;
		}
		
		// variables in the resulting potential
		ArrayList<Variable> unionVariables = (ArrayList<Variable>)
			variablesToEliminate.clone();
		unionVariables.addAll(variablesToKeep);
		int numUnionVariables = unionVariables.size();
		
		// current coordinate in the resulting potential
		int[] unionCoordinate = new int[numUnionVariables];
		int[] unionDimensions = TablePotential
			.calculateDimensions(unionVariables);
		
		// Defines some arrays for the proper potentials...
		double[][] tables = new double[numProperPotentials][];
		int[] initialPositions = new int[numProperPotentials];
		int[] currentPositions = new int[numProperPotentials];
		int[][] accumulatedOffsets = new int[numProperPotentials][];
		// ... and initializes them
		TablePotential unionPotential = new TablePotential(unionVariables,null);
		for (int i = 0; i < numProperPotentials; i++) {
			TablePotential potential = (TablePotential)properPotentials.get(i);
			tables[i] = potential.values;
			initialPositions[i] = potential.getInitialPosition();
			currentPositions[i] = initialPositions[i];
			accumulatedOffsets[i] = unionPotential
				.getAccumulatedOffsets(potential.getOriginalVariables());
		}
		
		// The result size is the product of the dimensions of the
		// variables to keeep
		int resultSize = resultingPotential.values.length;
		// The elimination size is the product of the dimensions of the
		// variables to eliminate
		int eliminationSize = 1;
		for (Variable variable : variablesToEliminate) {
			eliminationSize *= variable.getNumStates();
		}
		
		// Auxiliary variables for the nested loops
		double multiplicationResult; // product of the table values
		double accumulator; // in general, the sum or the maximum
		int increasedVariable = 0; // when computing the next configuration
		
		// outer iterations correspond to the variables to keep
		for (int outerIteration = 0; outerIteration < resultSize;
				outerIteration++) {
			// Inner iterations correspond to the variables to eliminate
			// accumulator summarizes the result of all inner iterations
		
			// first inner iteration
			multiplicationResult = constantFactor;
			for (int i = 0; i < numProperPotentials; i++) {
				// multiply the numbers
				multiplicationResult *= tables[i][currentPositions[i]];
			}
			accumulator = multiplicationResult;
		
			// next inner iterations
			for (int innerIteration = 1; innerIteration < eliminationSize;
					innerIteration++) {
		
				// find the next configuration and the index of the
				// increased
				// variable
				for (int j = 0; j < unionCoordinate.length; j++) {
					unionCoordinate[j]++;
					if (unionCoordinate[j] < unionDimensions[j]) {
						increasedVariable = j;
						break;
					}
					unionCoordinate[j] = 0;
				}
		
				// update the positions of the potentials we are multiplying
				for (int i = 0; i < numProperPotentials; i++) {
					currentPositions[i] +=
						accumulatedOffsets[i][increasedVariable];
				}
		
				// multiply the table values of the potentials
				multiplicationResult = constantFactor;
				for (int i = 0; i < numProperPotentials; i++) {
					multiplicationResult = multiplicationResult
							* tables[i][currentPositions[i]];
				}
		
				// update the accumulator (for this inner iteration)
				accumulator += multiplicationResult;
				// accumulator =
				// operator.combine(accumlator,multiplicationResult);
		
			} // end of inner iteration
		
			// when eliminationSize == 0 there is a multiplication without
			// marginalization but we must find the next configuration
			if (outerIteration < resultSize - 1) {
				// find the next configuration and the index of the
				// increased
				// variable
				for (int j = 0; j < unionCoordinate.length; j++) {
					unionCoordinate[j]++;
					if (unionCoordinate[j] < unionDimensions[j]) {
						increasedVariable = j;
						break;
					}
					unionCoordinate[j] = 0;
				}
		
				// update the positions of the potentials we are multiplying
				for (int i = 0; i < numProperPotentials; i++) {
					currentPositions[i] +=
						accumulatedOffsets[i][increasedVariable];
				}
			}
		
			resultingPotential.values[outerIteration] = accumulator;
		
		} // end of outer iteration
		
		return resultingPotential;
	}
    
	/** @param potentials potentials array to multiply
	 * @param variablesOfInterest Set of variables that must be kept (although 
	 * this set may contain some variables that are not in any potential)
	 * <code>potentials</code>
	 * @return The multiplied potentials
	 * @throws NotEnoughMemoryException */
	public static Potential multiplyAndMarginalize(
			ArrayList<Potential> potentials, 
			ArrayList<Variable> variablesOfInterest) 
			throws NotEnoughMemoryException {
	
		// Obtain parameters to invoke multiplyAndMarginalize
		// Union of the variables of the potential list
		ArrayList<Variable> unionVariables = 
			AuxiliaryOperations.getUnionVariables(potentials);
	
		// Classify unionVariables in two possibles arrays
		ArrayList<Variable> variablesToKeep =
			new ArrayList<Variable>();
		ArrayList<Variable> variablesToEliminate =
			new ArrayList<Variable>();
		for (Variable variable : unionVariables) {
			if (variablesOfInterest.contains(variable)) {
				variablesToKeep.add(variable);
			} else {
				variablesToEliminate.add(variable);
			}
		}
		
		return DiscretePotentialOperations.multiplyAndMarginalize(
			potentials, variablesToKeep, variablesToEliminate);
	}

	/** @param potentials <code>ArrayList</code> of <code>Potential</code>s to
	 * multiply.
	 * @param variableToEliminate <code>Variable</code>. 
	 * @return result <code>Potential</code> multiplied without 
	 * <code>variableToEliminate</code>
	 * @throws <code>NotEnoughMemoryException</code> */
	public static Potential multiplyAndMarginalize(
			ArrayList<? extends Potential> potentials, 
			Variable variableToEliminate) 
    		throws NotEnoughMemoryException {
    	ArrayList<Variable> variablesToEliminate = new ArrayList<Variable>();
    	variablesToEliminate.add(variableToEliminate);
    	ArrayList<Variable> variablesToKeep = new ArrayList<Variable>();
    	variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
    	variablesToKeep.remove(variableToEliminate);
    	return multiplyAndMarginalize(
    		potentials, variablesToKeep, variablesToEliminate);
    }
    
    /** @param potential <code>Potential</code> to marginalize
     * @param variableToEliminate <code>Variable</code>
     * @return Marginalized potential 
     * @throws NotEnoughMemoryException */
    @SuppressWarnings("unchecked")
	public static Potential marginalize(Potential potential, 
    		Variable variableToEliminate) throws NotEnoughMemoryException {
    	ArrayList<Variable> variablesToKeep = 
    		(ArrayList<Variable>)potential.getVariables().clone();
    	variablesToKeep.remove(variableToEliminate);
    	ArrayList<Variable> variablesToEliminate = new ArrayList<Variable>();
    	variablesToEliminate.add(variableToEliminate);
    	ArrayList<Potential> potentials = new ArrayList<Potential>();
    	potentials.add(potential);
    	return multiplyAndMarginalize(
    		potentials, variablesToKeep, variablesToEliminate);
    }
    
	/** @param potential
	 * @param variablesOfInterest
	 * @throws NotEnoughMemoryException */
	public static Potential marginalize(Potential potential,
			ArrayList<Variable> variablesOfInterest) 
			throws NotEnoughMemoryException {
	
		// Obtain parameters to invoke multiplyAndMarginalize
		// Union of the variables of the potential list
		ArrayList<Variable> variables = potential.getVariables();

		ArrayList<Variable> variablesToKeep =
			new ArrayList<Variable>();
		ArrayList<Variable> variablesToEliminate =
			new ArrayList<Variable>();
	
		for (Variable variable : variables) {
			if (variablesOfInterest.contains(variable)) {
				variablesToKeep.add(variable);
			} else {
				variablesToEliminate.add(variable);
			}
		}
	
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		potentials.add(potential);
	
		return DiscretePotentialOperations.multiplyAndMarginalize(
			potentials, variablesToKeep, variablesToEliminate);
	}

	/** @precondition variablesToKeep + variablesToEliminate =
	 * potential.getVariables()
	 * @precondition variablesToKeep
	 * @param potential that will be marginalized
	 * @param variablesToKeep
	 * @param variablesToEliminate
	 * @throws NotEnoughMemoryException 
	 * @throws PotentialOperationException */
	public static Potential marginalize(
			Potential potential,
			ArrayList<Variable> variablesToKeep,
			ArrayList<Variable> variablesToEliminate)
			throws NotEnoughMemoryException {
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		potentials.add(potential);
		return DiscretePotentialOperations.multiplyAndMarginalize(
			potentials, variablesToKeep, variablesToEliminate);
	}

	/** @param potentials An array of ordered <code>TablePotential</code>s
	 * @return constantFactor: The productory of the constant potentials (the
	 * first <i>k</i> because the array is ordered by size)
	 * @see org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations#getProperPotentials(ArrayList)
	 */
	public static double getConstantFactor(
			ArrayList<TablePotential> potentials) {
		double constantFactor = 1.0;
		for (TablePotential potential : potentials) {
			if (potential.values.length > 1) {
				continue;
			}
			constantFactor *= potential.values[0];
		}
		return constantFactor;
	}
	
	/** Compute the accumulated offsets of a <code>Potential</code>s array with
	 * the order imposed by <code>potentialResult</code>
	 * @param potentials <code>ArrayList</code> of <code>Potential</code>s.
	 * @param potentialResult <code>TablePotential</code>.
	 * @return An array of arrays of integers (<code>int[][]</code>). */
	public static int[][] getAccumulatedOffsets(
	        ArrayList<TablePotential> potentials,
	        TablePotential potentialResult) {

	    int numPotentials = potentials.size();
	    int[][] accumulatedOffsets = new int[numPotentials][];
	    
	    for (int i = 0; i < numPotentials; i++) {
	    	TablePotential potential = potentials.get(i);
	    	accumulatedOffsets[i] = potentialResult.getAccumulatedOffsets(
	    			potential.getOriginalVariables());
	    }
	    return accumulatedOffsets;
	}	
	
	/** @param potentials
	 * @param variablesToEliminate
	 * @throws NotEnoughMemoryException 
	 * @throws PotentialOperationException */
	public static Potential multiplyAndEliminate(
			ArrayList<Potential> potentials,
			ArrayList<Variable> variablesToEliminate) throws NotEnoughMemoryException {

		// Obtain parameters to invoke multiplyAndMarginalize
		// Union of the variables of the potential list
		ArrayList<Variable> variablesToKeep = 
			AuxiliaryOperations.getUnionVariables(potentials);
		variablesToKeep.removeAll(variablesToEliminate);
	
		return multiplyAndMarginalize(
			potentials, variablesToKeep, variablesToEliminate);
	}

	/** @param potential a <code>TablePotential</code>
	 * @return The <code>potential</code> normalized */
	public static TablePotential normalize(Potential potential) 
			throws NormalizeNullVectorException {
		TablePotential tablePotential = (TablePotential)potential;
		// Check for null vectors
		int p=0;
		for (p = 0; p < tablePotential.values.length; p++) {
			if (tablePotential.values[p] != 0.0) {
				break;
			}
		}
		if (p == tablePotential.values.length) {
			// All elements in tablePotential.table == 0 
			throw new NormalizeNullVectorException(
					"NormalizeNullVectorException: " + 
					"All elements in the TablePotential "
					+ tablePotential.getVariables() +
					" table are equal to 0.0");
		}
		ArrayList<Variable> variables = tablePotential.getVariables();
		if ((variables != null) && (variables.size() > 0)) {
			int positionLastVariable = variables.size() - 1;
			int numStatesLastVariable = ((Variable) variables
					.get(positionLastVariable)).getNumStates();
			int[] offsetsPotential = tablePotential.getOffsets();
			int offsetVariable = offsetsPotential[positionLastVariable];

			double normalizationFactor;
			for (int i = 0; i < offsetVariable; i++) {
				normalizationFactor = 0;
				for (int j = 0; j < numStatesLastVariable; j++) {
					normalizationFactor += 
						tablePotential.values[i + j * offsetVariable];
				}
				for (int j = 0; j < numStatesLastVariable; j++) {
					tablePotential.values[i + j * offsetVariable] /= 
						normalizationFactor;
				}
			}
		}
		return tablePotential;
	}

	/** Divides two <code>TablePotential</code>s using the accumulated offsets
	 * algorithm.
	 * @argCondition numerator and denominator have the same domain (variables)
	 * @param numerator <code>Potential</code>.
	 * @param denominator <code>Potential</code>.
	 * @return The quotient: A <code>TablePotential</code> with the union of the
	 * variables of numerator and denominator. */
	@SuppressWarnings("unchecked")
	public static Potential divide(Potential numerator, Potential denominator) {
		// Get variables and create quotient potential.
		// Quotient potential variables = numerator potential variables union
		// denominator potential variables
		TablePotential tNumerator = (TablePotential)numerator; 
		TablePotential tDenominator = (TablePotential)denominator;
		ArrayList<Variable> numeratorVariables = 
			(ArrayList<Variable>)tNumerator.getVariables().clone();
		ArrayList<Variable> denominatorVariables = 
			(ArrayList<Variable>)tDenominator.getVariables().clone();
		int numNumeratorVariables = numeratorVariables.size();
		int numDenominatorVariables = denominatorVariables.size();
		denominatorVariables.removeAll(numeratorVariables);
		numeratorVariables.addAll(denominatorVariables);
		ArrayList<Variable> quotientVariables = numeratorVariables;
		TablePotential quotient = null;
		try {
			quotient = new TablePotential(
					quotientVariables, PotentialRole.JOIN_PROBABILITY);
		} catch (NotEnoughMemoryException e) {
			ExceptionsHandler.handleException(e, null, false);
		}
		if ((numNumeratorVariables == 0) ||	(numDenominatorVariables == 0)) {
			return divide(tNumerator, tDenominator, quotient, 
				numNumeratorVariables, numDenominatorVariables);
		}
		
		int numVariables = quotient.getNumVariables();

		// Gets the tables of each TablePotential
		double[][] tables = new double[2][];
		tables[0] = tNumerator.values;
		tables[1] = tDenominator.values;

		// Gets dimension
		int[] quotientDimension = quotient.getDimensions();

		// Gets offset accumulate
		ArrayList<TablePotential> potentials = new ArrayList<TablePotential>();
		potentials.add(tNumerator);
		potentials.add(tDenominator);
		int[][]offsetAccumulate = DiscretePotentialOperations
			.getAccumulatedOffsets(potentials, quotient);

		// Gets coordinate
		int[] quotientCoordinate;
		if (numVariables != 0) {
			quotientCoordinate = new int[numVariables];
		} else {
			quotientCoordinate = new int[1];
			quotientCoordinate[0] = 0;
		}

		// Position in each table potential
		int[] potentialsPositions = new int[2];
		for (int i = 0; i < 2; i++) {
			potentialsPositions[i] = 0;
		}

		// Divide
		int incrementedVariable = 0;
		int[] dimension = quotient.getDimensions();
		int[] offset = quotient.getOffsets();
		int tamTable = 1; // If numVariables == 0 the potential is a constant
		if (numVariables > 0) {
		    tamTable = dimension[numVariables-1] * offset[numVariables-1];
		}

		for (int quotientPosition=0; quotientPosition < tamTable; 
				quotientPosition++) {
			/* increment the result coordinate and
			   find out which variable is to be incremented */
			for (int iVariable = 0; iVariable < quotientCoordinate.length;
					iVariable++) {
				// try by incrementing the current variable (given by iVariable)
				quotientCoordinate[iVariable]++;
				if (quotientCoordinate[iVariable] != 
					    quotientDimension[iVariable]) {
					// we have incremented the right variable
					incrementedVariable = iVariable;
					// do not increment other variables;
					break;
				}
				/* this variable could not be incremented;
				   we set it to 0 in resultCoordinate
				   (the next iteration of the for-loop will increment
				   the next variable) */
				quotientCoordinate[iVariable] = 0;
			}

			// divide
			if (tDenominator.values[potentialsPositions[1]] == 0.0) {
				quotient.values[quotientPosition] = 0.0;
			} else {
				quotient.values[quotientPosition] = 
					tNumerator.values[potentialsPositions[0]] / 
					tDenominator.values[potentialsPositions[1]];
			}
			for (int iPotential=0; iPotential < 2; iPotential++) {
				// update the current position in each potential table
				potentialsPositions[iPotential] +=
					offsetAccumulate[iPotential][incrementedVariable];
			}
		}
		
		return quotient;
	}

	/** Divide two potentials when one of them has any variable
	 * @param numerator <code>TablePotential</code>
	 * @param denominator <code>TablePotential</code>
	 * @param quotient <code>TablePotential</code>
	 * @param numNumeratorVariables <code>int</code>
	 * @param numDenominatorVariables <code>int</code>
	 * @return quotient The <code>TablePotential</code> received with its table.
	 */
	private static Potential divide(TablePotential numerator, 
			TablePotential denominator,	TablePotential quotient, 
			int numNumeratorVariables, int numDenominatorVariables) {
		if (numNumeratorVariables == 0) {
			int sizeTableDenominator = denominator.values.length;
			double dNumerator = numerator.values[0];
			for (int i = 0; i < sizeTableDenominator; i++) {
				quotient.values[i] = dNumerator / denominator.values[i];
			}
		} else {
			int sizeTableNumerator = numerator.values.length;
			double dDenominator = denominator.values[0];
			for (int i = 0; i < sizeTableNumerator; i++) {
				quotient.values[i] = numerator.values[i] / dDenominator;
			}			
		}
		return quotient;
	}

	// TODO Eliminar este método si no es usado por otros
	/** @param numerator <tt>Potential</tt>
	 * @param denominator <tt>Potential</tt>
	 * @throws <tt>IllegalArgumentTypeException</tt> if numerator of denominator
	 * are not <tt>TablePotential</tt> 
	 * @return The quotient */
	public static Potential dividePotentials(Potential numerator, 
			Potential denominator) throws IllegalArgumentTypeException {
		// parameter correct type verification before calling right method
		if (!(numerator instanceof TablePotential) || 
                !(denominator instanceof TablePotential)) {
            String errMsg = new String("");
            errMsg = errMsg + "Unsupported operation: " +
                "divide can only manage potentials of type TablePotential.\n";
            if (numerator == null) {
                errMsg = errMsg + "Numerator = null\n";
            } else {
                if (!(numerator instanceof TablePotential)) {
                    errMsg = errMsg + "Numerator class is " 
                        + numerator.getClass().getName() + "\n";
                }
            }
            if (denominator == null) {
                errMsg = errMsg + "Denominator = null\n";
            } else {
                if (!(denominator instanceof TablePotential)) {
                    errMsg = errMsg + "Denominator class is " 
                        + denominator.getClass().getName() + "\n";
                }
            }
			throw new IllegalArgumentTypeException(errMsg);    						
		}
		
		return DiscretePotentialOperations.divide(numerator, denominator);
	}

	/** @param tablePotentials <code>ArrayList</code> of
	 * <code>TablePotential</code>s.
	 * @param fsVariablesToKeep <code>ArrayList</code> of
	 * <code>Variable</code>s.
	 * @param fsVariableToMaximize <code>Variable</code>.
	 * @return Two potentials: 1) a <code>Potential</code> resulting of 
	 * multiplication and maximization of <code>variableToMaximize</code> and 2)
	 * a <code>GTablePotential</code> of <code>Choice</code> (same variables as
	 * preceding) with the value choosed for <code>variableToMaximize</code> in
	 * each configuration.
	 * @throws <code>NotEnoughMemoryException</code> */
    @SuppressWarnings("unchecked")
	public static Object[] multiplyAndMaximize(
            ArrayList<Potential> tablePotentials, 
            ArrayList<Variable> fSVariablesToKeep,
            Variable fSVariableToMaximize) 
            throws NotEnoughMemoryException {
        ArrayList<TablePotential> potentials = 
            (ArrayList<TablePotential>)((Object)tablePotentials);
        
        ArrayList<Variable> variablesToKeep = 
            (ArrayList<Variable>)((Object)fSVariablesToKeep);

        PotentialRole role = getRole(tablePotentials);

        TablePotential resultingPotential = 
        	new TablePotential(variablesToKeep, role);
        
        GTablePotential<Choice> gResult = new GTablePotential<Choice>(
        		variablesToKeep, role);
        int numStates = ((Variable)fSVariableToMaximize).getNumStates();
        int[] statesChoosed;
        Choice choice;
        
        // Constant potentials are those that do not depend on any variables.
        // The product of all the constant potentials is the constant factor.
        double constantFactor = 1.0;
        // Non constant potentials are proper potentials.
        ArrayList<TablePotential> properPotentials =
            new ArrayList<TablePotential>();
        for (Potential potential : potentials) {
            if (potential.getNumVariables() != 0) {
                properPotentials.add((TablePotential) potential);
            } else {
                constantFactor *= ((TablePotential)potential)
                    .values[((TablePotential) potential).getInitialPosition()];
            }
        }
        
        int numProperPotentials = properPotentials.size();
        
        if (numProperPotentials == 0) {
            resultingPotential.values[0] = constantFactor;
            return new Object[]{resultingPotential, gResult};
        }
        
        // variables in the resulting potential
        ArrayList<Variable> unionVariables = new ArrayList<Variable>();
        unionVariables.add((Variable)fSVariableToMaximize);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();
        
        // current coordinate in the resulting potential
        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential
            .calculateDimensions(unionVariables);
        
        // Defines some arrays for the proper potentials...
        double[][] tables = new double[numProperPotentials][];
        int[] initialPositions = new int[numProperPotentials];
        int[] currentPositions = new int[numProperPotentials];
        int[][] accumulatedOffsets = new int[numProperPotentials][];
        // ... and initializes them
        TablePotential unionPotential = new TablePotential(unionVariables,null);
        for (int i = 0; i < numProperPotentials; i++) {
            TablePotential potential = (TablePotential)properPotentials.get(i);
            tables[i] = potential.values;
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = unionPotential
                .getAccumulatedOffsets(potential.getOriginalVariables());
        }
        
        // The result size is the product of the dimensions of the
        // variables to keeep
        int resultSize = resultingPotential.values.length;
        // The elimination size is the product of the dimensions of the
        // variables to eliminate
        int eliminationSize = 1;
        eliminationSize *= ((Variable)fSVariableToMaximize).getNumStates();
        
        // Auxiliary variables for the nested loops
        double multiplicationResult; // product of the table values
        double accumulator; // in general, the sum or the maximum
        int increasedVariable = 0; // when computing the next configuration
        
        // outer iterations correspond to the variables to keep
        for (int outerIteration = 0; outerIteration < resultSize;
                outerIteration++) {
            // Inner iterations correspond to the variables to eliminate
            // accumulator summarizes the result of all inner iterations
        
            // first inner iteration
            multiplicationResult = constantFactor;
            for (int i = 0; i < numProperPotentials; i++) {
                // multiply the numbers
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            statesChoosed = new int[numStates];
            statesChoosed[0] = 0;
            choice = new Choice(fSVariableToMaximize, statesChoosed);
            accumulator = multiplicationResult;
            choice.setValue(0); // because in first iteration we have a maximum
        
            // next inner iterations
            for (int innerIteration = 1; innerIteration < eliminationSize;
                    innerIteration++) {
        
                // find the next configuration and the index of the
                // increased variable
                for (int j = 0; j < unionCoordinate.length; j++) {
                    unionCoordinate[j]++;
                    if (unionCoordinate[j] < unionDimensions[j]) {
                        increasedVariable = j;
                        break;
                    }
                    unionCoordinate[j] = 0;
                }
        
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] +=
                        accumulatedOffsets[i][increasedVariable];
                }
        
                // multiply the table values of the potentials
                multiplicationResult = constantFactor;
                for (int i = 0; i < numProperPotentials; i++) {
                    multiplicationResult = multiplicationResult
                            * tables[i][currentPositions[i]];
                }
        
                // update the accumulator (for this inner iteration)
                if (multiplicationResult > (accumulator + maxRoundErrorAllowed)) 
                {
                	choice.setValue(innerIteration);
                    accumulator = multiplicationResult;
                } else {
                	if ((multiplicationResult < 
                			 (accumulator + maxRoundErrorAllowed)) &&
                			(multiplicationResult >= 
                			 (accumulator - maxRoundErrorAllowed))) {
                		choice.addValue(innerIteration);
                	}
                }
                // accumulator =
                // operator.combine(accumlator,multiplicationResult);
        
            } // end of inner iteration
        
            // when eliminationSize == 0 there is a multiplication without
            // maximization but we must find the next configuration
            if (outerIteration < resultSize - 1) {
                // find the next configuration and the index of the
                // increased variable
                for (int j = 0; j < unionCoordinate.length; j++) {
                    unionCoordinate[j]++;
                    if (unionCoordinate[j] < unionDimensions[j]) {
                        increasedVariable = j;
                        break;
                    }
                    unionCoordinate[j] = 0;
                }
        
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] +=
                        accumulatedOffsets[i][increasedVariable];
                }
            }
        
            resultingPotential.values[outerIteration] = accumulator;
            gResult.elementTable.add(choice);
        
        } // end of outer iteration
        
        Object[] resultPotentials = {resultingPotential, gResult};
        return resultPotentials;
    }

	/** @param potentialsVariable <code>ArrayList</code> of 
	 *   <code>Potential</code>s to multiply.
	 * @param variableToMaximize <code>Variable</code>. 
	 * @return Two potentials: 1) a <code>Potential</code> resulting of 
	 *   multiplication and maximization of <code>variableToMaximize</code> and
	 *   2) a <code>GTablePotential</code> of <code>Choice</code> (same 
	 *   variables as preceding) with the value choosed for 
	 *   <code>variableToMaximize</code> in each configuration.
	 * @throws <code>NotEnoughMemoryException</code>. */
	public static Object[] multiplyAndMaximize(
			ArrayList<Potential> potentialsVariable, 
			Variable variableToMaximize) 
			throws NotEnoughMemoryException {
		// Use a HashSet to add the variables to avoid adding one variable more
		// than one time
    	HashSet<Variable> addedVariables = new HashSet<Variable>();
    	for (Potential potential : potentialsVariable) {
    		addedVariables.addAll(potential.getVariables());
    	}
    	ArrayList<Variable> variablesToKeep = 
    		new ArrayList<Variable>(addedVariables);
    	variablesToKeep.remove(variableToMaximize);
    	return multiplyAndMaximize(
   		    potentialsVariable, variablesToKeep, variableToMaximize);
	}
	
	/** @param potential one <code>TablePotential</code>.
	 * @param variableToMaximize <code>Variable</code>.
	 * @return Two potentials: 1) a <code>Potential</code> resulting of
	 *   multiplication and maximization of <code>variableToMaximize</code> and
	 *   2) a <code>GTablePotential</code> of <code>Choice</code> (same 
	 *   variables as preceding) with the value choosed for 
	 *   <code>variableToMaximize</code> in each configuration.
	 * @throws <code>NotEnoughMemoryException</code>. */
	@SuppressWarnings("unchecked")
	public static Object[] maximize(Potential potential, 
			Variable variableToMaximize) throws NotEnoughMemoryException {
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		potentialsVariable.add(potential);
		ArrayList<Variable> variablesToKeep = 
			(ArrayList<Variable>)potential.getVariables().clone();
		variablesToKeep.remove(variableToMaximize);
		return multiplyAndMaximize(
   		    potentialsVariable, variablesToKeep, variableToMaximize);
	}

	/** Copy the potential received to another potential with the same variables
	 *   but with the ordination received in <code>otherVariables</code>
	 * @param potential <code>TablePotential</code>
	 * @param orderVariables <code>ArrayList</code> of  <code>Variable</code>
	 * @return The <code>TablePotential</code> generated
	 * @argCondition <code>otherVariables</code> are the same variables than the
	 *   variables of <code>potential</code>
	 * @throws <code>NotEnoughMemoryException</code> */
	public static TablePotential reorder(TablePotential potential,
			ArrayList<Variable> orderVariables) throws NotEnoughMemoryException{
		TablePotential copyPotential = 
			new TablePotential(orderVariables, potential.getPotentialRole());
		int[] accOffsets = potential.getAccumulatedOffsets(orderVariables);
		int[] potentialPositions = new int[potential.getNumVariables()];
		int[] potentialDimensions = potential.getDimensions();
		double[] tablePotential = potential.values;
		double[] tableCopyPotential = copyPotential.values;
		int copyTablePosition = 0;
		int numVariables = orderVariables.size();
		int incrementedVariable, i;
		for (i = 0; i < tablePotential.length - 1; i++) {
			tableCopyPotential[copyTablePosition] = tablePotential[i];
	
			for (incrementedVariable = 0; incrementedVariable < numVariables;
					incrementedVariable++) {
				potentialPositions[incrementedVariable]++;
				if (potentialPositions[incrementedVariable] ==
						potentialDimensions[incrementedVariable]) {
					potentialPositions[incrementedVariable] = 0;
				} else {
					break;
				}
			}
			copyTablePosition += accOffsets[incrementedVariable];
		}
		tableCopyPotential[copyTablePosition] = tablePotential[i];
		if (potential.isUtility()) {
			copyPotential.setUtilityVariable(potential.getUtilityVariable());
		}
		copyPotential.properties = potential.properties;
		return copyPotential;
	}
	
}
