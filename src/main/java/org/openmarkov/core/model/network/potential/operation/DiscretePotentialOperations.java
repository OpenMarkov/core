/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.exception.DivideByZeroException;
import org.openmarkov.core.exception.IllegalArgumentTypeException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * This class defines a set of common operations over discrete potentials (
 * <code>TablePotential</code>s) and discrete variables (<code>Variable</code>
 * s). The method are invoked from <code>PotentialOperations</code> after
 * checking that the parameters are discrete.
 * 
 * @author marias
 */
public final class DiscretePotentialOperations {

    /**
     * Round error used to compare two numbers. If they differ in less than
     * <code>maxRoundErrorAllowed</code> they will be considered equals.
     */
    public static double maxRoundErrorAllowed = 1E-8;
    
	
    /**
     * @param tablePotentials
     *            <code>ArrayList</code> of extends <code>Potential</code>.
     * @return A <code>TablePotential</code> as result.
     * @throws WrongCriterionException
     * @throws NonProjectablePotentialException
     */
    public static TablePotential multiply(List<TablePotential> tablePotentials) {
        return multiply(tablePotentials, true);
    }

    /**
     * @param tablePotentials
     *            <code>ArrayList</code> of extends <code>Potential</code>.
     * @param reorder. Sorts or not the potentials prior to multiplication.
     *            <code>boolean</code>.
     * @return A <code>TablePotential</code> as result.
     */
    public static TablePotential multiply(List<TablePotential> tablePotentials, boolean reorder) {
        int numPotentials = tablePotentials.size();
        // Special cases: one or zero potentials
        if (numPotentials < 2) {
            if (numPotentials == 1) {
                return (TablePotential) tablePotentials.get(0);
            } else {
                return null;
            }
        }

        List<TablePotential> potentials = new ArrayList<>(tablePotentials);

        // Sort the potentials according to the table size
        if (reorder) {
            Collections.sort(potentials);
        }

        // Gets constant factor: The product of constant potentials
        double constantFactor = getConstantFactor(potentials);

        // get role
        PotentialRole role = getRole(potentials);

        potentials = AuxiliaryOperations.getNonConstantPotentials(potentials);
        if (potentials.size() == 0) {
            TablePotential constantTablePotential = new TablePotential(null, role);
            constantTablePotential.values[0] = constantFactor;
            return constantTablePotential;
        }

        // Gets the union
        List<Variable> resultVariables = AuxiliaryOperations.getUnionVariables(potentials);

        int numVariables = resultVariables.size();

        // Gets the tables of each TablePotential
        numPotentials = potentials.size();
        double[][] tables = new double[numPotentials][];
        for (int i = 0; i < numPotentials; i++) {
            tables[i] = potentials.get(i).values;
        }

        // Gets dimension
        int[] resultDimension = TablePotential.calculateDimensions(resultVariables);

        // Gets offset accumulate
        int[][] offsetAccumulate = DiscretePotentialOperations.getAccumulatedOffsets(potentials,
                resultVariables);

        // Gets coordinate
        int[] resultCoordinate = initializeCoordinates(numVariables);
        
        // Position in each table potential
        int[] potentialsPositions = new int[numPotentials];
        for (int i = 0; i < numPotentials; i++) {
            potentialsPositions[i] = 0;
        }

        // Multiply
        int incrementedVariable = 0;
        double mulResult;
        int[] dimensions = TablePotential.calculateDimensions(resultVariables);
        int[] offsets = TablePotential.calculateOffsets(dimensions);
        int tableSize = numVariables > 0 ? dimensions[numVariables - 1] * offsets[numVariables - 1] : 1;
        double[] resultValues = new double[tableSize];

        for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
            mulResult = constantFactor;

            //increment the result coordinate and find out which variable is to be incremented
            for (int iVariable = 0; iVariable < resultCoordinate.length; iVariable++) {
                // try by incrementing the current variable (given by iVariable)
                resultCoordinate[iVariable]++;
                if (resultCoordinate[iVariable] != resultDimension[iVariable]) {
                    // we have incremented the right variable
                    incrementedVariable = iVariable;
                    // do not increment other variables;
                    break;
                }
                /*
                 * this variable could not be incremented; we set it to 0 in
                 * resultCoordinate (the next iteration of the for-loop will
                 * increment the next variable)
                 */
                resultCoordinate[iVariable] = 0;
            }

            // multiply
            for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
                // multiply the numbers
                mulResult = mulResult * tables[iPotential][potentialsPositions[iPotential]];
                // update the current position in each potential table
                potentialsPositions[iPotential] += offsetAccumulate[iPotential][incrementedVariable];
            }

            resultValues[resultPosition] = mulResult;
        }
        return new TablePotential(resultVariables, role, resultValues);
    }
    
   
    /**
     * @param tablePotentials <code>List</code> of <code>TablePotential</code>s.
     * @return <code>TablePotential</code>
     */
    public static TablePotential sum(List<TablePotential> tablePotentials) {
    	if (tablePotentials == null || tablePotentials.size() == 0) {
    		return new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.0});
    	}
        if (tablePotentials.size() == 1) {
            return (TablePotential) tablePotentials.get(0);
        }

        // list of non-constant potentials
        List<TablePotential> potentials = new ArrayList<>(tablePotentials);

        // Leave out the constant potentials
        List<TablePotential> constantPotentials = new ArrayList<TablePotential>();
        for (int i = 0; i < tablePotentials.size(); i++) {
            Potential auxPotential = tablePotentials.get(i);
            if (auxPotential.getVariables().size() == 0) {
                potentials.remove(auxPotential);
                constantPotentials.add((TablePotential) auxPotential);
            }
        }

        // Calculate the sum of constant potentials
        double sumConstantPotentials = 0.0;
        Intervention constantPotentialsIntervention = null;
        int numConstantPotentials = constantPotentials.size();
        for (int i = 0; i < numConstantPotentials; i++) {
            sumConstantPotentials += constantPotentials.get(i).values[0];
            Intervention[] iConstantPotentialInterventions = constantPotentials.get(i).interventions;
			if (iConstantPotentialInterventions != null) {
            	Intervention onlyInterventionIConstantPotential = iConstantPotentialInterventions[0];
            	constantPotentialsIntervention = (constantPotentialsIntervention == null)?onlyInterventionIConstantPotential:
            		constantPotentialsIntervention.concatenate(onlyInterventionIConstantPotential);            	
            }
        }

        // From here operate only with non constant potentials
        int numPotentials = potentials.size();

        // Gets the union
        List<Variable> resultVariables = AuxiliaryOperations.getUnionVariables(potentials);
        int numVariables = resultVariables.size();

        // Gets the tables of each TablePotential
        double[][] tables = new double[numPotentials][];
        for (int i = 0; i < numPotentials; i++) {
            tables[i] = potentials.get(i).values;
        }

        // Gets the interventions if necessary
        boolean thereAreInterventions = false;
        for (int i = 0; i < numPotentials && !thereAreInterventions; i++) {
        	thereAreInterventions = (potentials.get(i).interventions != null);
        }
        Intervention[][] interventions = null;
        if (thereAreInterventions) {
        	interventions = new Intervention[numPotentials][];
        	for (int i = 0; i < numPotentials; i++) {
        		interventions[i] = potentials.get(i).interventions;
        	}
        }

        // Gets the dimensions
        int[] resultDimensions = TablePotential.calculateDimensions(resultVariables);

        // Gets the accumulated offsets
        int[][] accumulatedOffsets = DiscretePotentialOperations.getAccumulatedOffsets(potentials,
                resultVariables);

        // Gets the coordinates
        int[] resultCoordinates = initializeCoordinates(numVariables);

        // Position in each table potential
        int[] potentialPositions = new int[numPotentials];
        for (int i = 0; i < numPotentials; i++) {
            potentialPositions[i] = 0;
        }

        // Sum
        int incrementedVariable = 0;
        boolean resultVariablesNotEmpty = !resultVariables.isEmpty();
		int[] dimensions = resultVariablesNotEmpty ? TablePotential.calculateDimensions(resultVariables)
                : new int[0];
        int[] offsets = resultVariablesNotEmpty ? TablePotential.calculateOffsets(dimensions)
                : new int[0];
        int tableSize = 1; // If numVariables == 0 the potential is a constant
        if (numVariables > 0) {
            tableSize = dimensions[numVariables - 1] * offsets[numVariables - 1];
        }
        double[] resultValues = new double[tableSize];
		Intervention[] resultInterventions = (thereAreInterventions || constantPotentialsIntervention != null) ? new Intervention[tableSize]
				: null;

        if (potentials.size() > 0) {
            double sum;
            for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
                /*
                 * increment the result coordinate and find out which variable
                 * is to be incremented
                 */
                for (int iVariable = 0; iVariable < resultCoordinates.length; iVariable++) {
                    // try by incrementing the current variable (given by
                    // iVariable)
                    resultCoordinates[iVariable]++;
                    if (resultCoordinates[iVariable] != resultDimensions[iVariable]) {
                        // we have incremented the right variable
                        incrementedVariable = iVariable;
                        // do not increment other variables;
                        break;
                    }
                    /*
                     * this variable could not be incremented; we set it to 0 in
                     * resultCoordinate (the next iteration of the for-loop will
                     * increment the next variable)
                     */
                    resultCoordinates[iVariable] = 0;
                }

                // sum
                sum = 0;
                Intervention resultIntervention = null;
                for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
                    // sum the numbers
                    sum = sum + tables[iPotential][potentialPositions[iPotential]];
					if (thereAreInterventions && interventions[iPotential] != null) {
						Intervention auxIIntervention = interventions[iPotential][potentialPositions[iPotential]];
						resultIntervention = (resultIntervention == null) ? auxIIntervention
								: resultIntervention.concatenate(auxIIntervention);
					}
                    
                    // update the current position in each potential table
                    potentialPositions[iPotential] += accumulatedOffsets[iPotential][incrementedVariable];
                }                
                resultValues[resultPosition] = sum;
                if (thereAreInterventions) {
                	resultInterventions[resultPosition] = resultIntervention;
                }
            }
        }
        // Sum constant potentials to the result
        if ((numConstantPotentials > 0) && (sumConstantPotentials != 0.0 || constantPotentialsIntervention != null)) {
            for (int i = 0; i < resultValues.length; i++) {
                resultValues[i] = resultValues[i] + sumConstantPotentials;
                if (constantPotentialsIntervention != null) {
                	if (resultInterventions[i] == null) {
                		resultInterventions[i] = constantPotentialsIntervention;
                	} else {
                		resultInterventions[i].concatenate(constantPotentialsIntervention);
                	}
                }
            }
        }
        TablePotential result = new TablePotential(resultVariables, getRole(tablePotentials), resultValues);
        result.interventions = resultInterventions;
        // TODO Ver si esto es eliminable
        if (result.isUtility()) {
        	result.setUtilityVariable(getNewUtilityVariable(tablePotentials));
        }
        return result;
    }

	private static int[] initializeCoordinates(int numVariables) {
		int[] resultCoordinates = new int[Math.max(1,numVariables)];
        if (numVariables == 0) {
        	resultCoordinates[0] = 0;
        }
		return resultCoordinates;
	}

    
	/** Given a collection of variables, creates a new variable whose name is the concatenation 
     * of the names of the other variables. If there is only one, returns that one. If the collection is empty,
     * returns a new variable with name "U"
     * @param variables
     * @return <code>Variable</code>
     */
    private static Variable composeVariable(Collection<Variable> variables) {
    	Variable finalVariable = null;
		String name = "";
		if (variables.isEmpty()) {
			name = "U";
			finalVariable = new Variable(name);
		} else {
			if (variables.size() > 1) {
				int i = 0;
				int size = variables.size();
				for (Variable variable : variables) {
					i++;
					name = name + variable.getName();
					if (i < size) {
						name = name + "-";
					}
				}
				finalVariable = new Variable(name);
			} else {
				for (Variable variable : variables) {
					finalVariable = variable;
				}
			}
		}
		return finalVariable;
    }
    
	/**
	 * @param tablePotentials
	 * @return A new variable whose name is the concatenation of the names of the utility variables.
	 */
	private static Variable getNewUtilityVariable(List<TablePotential> tablePotentials) {
		Set<Variable> utilityVariables = new HashSet<Variable>();
		for (TablePotential potential : tablePotentials) {
			if (potential.isUtility()) {
				Variable utilityVariable = potential.getUtilityVariable();
				if (utilityVariable != null) {
					utilityVariables.add(utilityVariable);
				}
			}
		}
		return composeVariable(utilityVariables);
	}

//	/**
//     * @param potentials. <code>TablePotential</code>
//     * @return <code>true</code> when at least one potential has an array of interventions.
//     */
//    private static boolean anyPotentialWithInterventions(List<TablePotential> potentials) {
//    	int i;
//    	for (i = 0; i < potentials.size() && potentials.get(i).interventions == null; i++);
//		return i < potentials.size();
//	}
//
//	/**
//	 * @param result. <code>TablePotential</code>
//	 * @param allThePotentials. <code>List</code> of <code>TablePotential</code>
//	 * @return result with the interventions. <code>TablePotential</code>
//	 */
//	private static TablePotential sumInterventions(TablePotential result, List<TablePotential> allThePotentials) {
//		result.interventions = new Intervention[result.values.length];
//		List<TablePotential> potentials = new ArrayList<TablePotential>();
//		for (TablePotential potential : allThePotentials) {
//			if (potential.interventions != null) {
//				potentials.add(potential);
//			}
//		}
//		int numPotentials = potentials.size();
//
//		// Gets the tables of each TablePotential
//        Intervention[][] interventionTables = new Intervention[numPotentials][];
//        for (int i = 0; i < numPotentials; i++) {
//            interventionTables[i] = potentials.get(i).interventions;
//        }
//        
//        List<Variable> resultVariables = result.getVariables();
//
//        // Gets dimensions
//        int[] resultDimensions = TablePotential.calculateDimensions(resultVariables);
//
//        // Gets accumulated offsets
//        int[][] accumulatedOffsets = DiscretePotentialOperations.getAccumulatedOffsets(potentials,
//                resultVariables);
//        
//        int numVariables = resultVariables.size();
//
//        // Gets coordinate
//        int[] resultCoordinates;
//        if (numVariables != 0) {
//            resultCoordinates = new int[numVariables];
//        } else {
//            resultCoordinates = new int[1];
//            resultCoordinates[0] = 0;
//        }
//
//        // Position in each table potential
//        int[] potentialPositions = new int[numPotentials];
//        for (int i = 0; i < numPotentials; i++) {
//            potentialPositions[i] = 0;
//        }
//
//        // Sum
//        int incrementedVariable = 0;
//        int[] dimensions = (!resultVariables.isEmpty()) ? TablePotential.calculateDimensions(resultVariables)
//                : new int[0];
//        int[] offsets = (!resultVariables.isEmpty()) ? TablePotential.calculateOffsets(dimensions)
//                : new int[0];
//        int tableSize = 1; // If numVariables == 0 the potential is a constant
//        if (numVariables > 0) {
//            tableSize = dimensions[numVariables - 1] * offsets[numVariables - 1];
//        }
//        Intervention[] resultInterventions = new Intervention[tableSize];
//
//        if (allThePotentials.size() > 0) {
//            Intervention sum = null;
//            for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
//                /*
//                 * increment the result coordinate and find out which variable
//                 * is to be incremented
//                 */
//                for (int iVariable = 0; iVariable < resultCoordinates.length; iVariable++) {
//                    // try by incrementing the current variable (given by
//                    // iVariable)
//                    resultCoordinates[iVariable]++;
//                    if (resultCoordinates[iVariable] != resultDimensions[iVariable]) {
//                        // we have incremented the right variable
//                        incrementedVariable = iVariable;
//                        // do not increment other variables;
//                        break;
//                    }
//                    /*
//                     * this variable could not be incremented; we set it to 0 in
//                     * resultCoordinate (the next iteration of the for-loop will
//                     * increment the next variable)
//                     */
//                    resultCoordinates[iVariable] = 0;
//                }
//
//                // sum
//                sum = null;
//                for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
//                    // sum the numbers
//                	Intervention intervention = interventionTables[iPotential][potentialPositions[iPotential]];
//                	if (sum == null) {
//                		sum = intervention; 
//                	} else {
//                		sum.concatenate(intervention);
//                	}
//                    // update the current position in each potential table
//                    potentialPositions[iPotential] += accumulatedOffsets[iPotential][incrementedVariable];
//                }                
//                resultInterventions[resultPosition] = sum;
//            }
//        }
//		
//		return result;
//	}

	public static TablePotential sum(TablePotential... tablePotentials) {
        List<TablePotential> potentialList = new ArrayList<TablePotential>(tablePotentials.length);
        for (TablePotential potential : tablePotentials) {
            potentialList.add(potential);
        }
        return sum(potentialList);
    }

    /**
     * @param potentials
     * @return
     */
    public static PotentialRole getRole(List<? extends Potential> potentials) {
        boolean atLeastOneUtility = false;
        for (Potential potential : potentials) {
            atLeastOneUtility = atLeastOneUtility || potential.isUtility();
        }
        if (atLeastOneUtility) {
            return PotentialRole.UTILITY;
        }
        boolean atLeastOneJoinProb = false;
        for (Potential potential : potentials) {
            atLeastOneJoinProb = atLeastOneJoinProb
                    || potential.getPotentialRole() == PotentialRole.JOINT_PROBABILITY;
        }
        if (atLeastOneJoinProb) {
            return PotentialRole.JOINT_PROBABILITY;
        }
        return PotentialRole.CONDITIONAL_PROBABILITY;
    }

    /**
     * @param tablePotentials
     *            array to multiply
     * @param variablesToKeep
     *            The set of variables that will appear in the resulting
     *            potential
     * @param variablesToEliminate
     *            The set of variables eliminated by marginalization (in
     *            general, by summing out or maximizing)
     * @argCondition variablesToKeep and variablesToEliminate are a partition of
     *               the union of the variables of the potential
     * @return A <code>TablePotential</code> result of multiply and marginalize.
     */
    public static TablePotential multiplyAndMarginalize(List<TablePotential> tablePotentials,
            List<Variable> variablesToKeep,
            List<Variable> variablesToEliminate) {

        // Constant potentials are those that do not depend on any variables.
        // The product of all the constant potentials is the constant factor.
        double constantFactor = 1.0;
        // Non constant potentials are proper potentials.
        List<TablePotential> nonConstantPotentials = new ArrayList<TablePotential>();
        for (TablePotential potential : tablePotentials) {
            if (potential.getNumVariables() != 0) {
                nonConstantPotentials.add(potential);
            } else {
                constantFactor *= potential.values[potential.getInitialPosition()];
            }
        }

        int numNonConstantPotentials = nonConstantPotentials.size();

        if (numNonConstantPotentials == 0) {
            TablePotential resultingPotential = new TablePotential(variablesToKeep,
                    getRole(tablePotentials));
            resultingPotential.values[0] = constantFactor;
            return resultingPotential;
        }

        // variables in the resulting potential
        List<Variable> unionVariables = new ArrayList<Variable>(variablesToEliminate);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        // current coordinate in the resulting potential
        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        // Defines some arrays for the proper potentials...
        double[][] tables = new double[numNonConstantPotentials][];
        int[] initialPositions = new int[numNonConstantPotentials];
        int[] currentPositions = new int[numNonConstantPotentials];
        int[][] accumulatedOffsets = new int[numNonConstantPotentials][];
        // ... and initializes them
        for (int i = 0; i < numNonConstantPotentials; i++) {
            TablePotential potential = nonConstantPotentials.get(i);
            tables[i] = potential.values;
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = TablePotential.getAccumulatedOffsets(unionVariables, potential.getVariables());
        }

        // The result size is the product of the dimensions of the
        // variables to keep
        int resultSize = TablePotential.computeTableSize(variablesToKeep);
        double[] resultValues = new double[resultSize];
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
        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            // Inner iterations correspond to the variables to eliminate
            // accumulator summarizes the result of all inner iterations

            // first inner iteration
            multiplicationResult = constantFactor;
            for (int i = 0; i < numNonConstantPotentials; i++) {
                // multiply the numbers
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            accumulator = multiplicationResult;

            // next inner iterations
            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {

                // find the next configuration and the index of the
                // increased variable
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);

                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numNonConstantPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                // multiply the table values of the potentials
                multiplicationResult = constantFactor;
                for (int i = 0; i < numNonConstantPotentials; i++) {
                    multiplicationResult *= tables[i][currentPositions[i]];
                }

                // update the accumulator (for this inner iteration)
                accumulator += multiplicationResult;
                // accumulator =
                // operator.combine(accumulator,multiplicationResult);

            } // end of inner iteration

            // when eliminationSize == 0 there is a multiplication without
            // marginalization but we must find the next configuration
            if (outerIteration < resultSize - 1) {
                // find the next configuration and the index of the
                // increased variable
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);
                
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numNonConstantPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultValues[outerIteration] = accumulator;

        } // end of outer iteration

        return new TablePotential(variablesToKeep, getRole(tablePotentials), resultValues);
    }

    /**
     * @param potentials
     *            potentials array to multiply
     * @param variablesOfInterest
     *            Set of variables that must be kept (although this set may
     *            contain some variables that are not in any potential)
     *            <code>potentials</code>
     * @return The multiplied potentials
     */
    public static TablePotential multiplyAndMarginalize(List<TablePotential> potentials,
            List<Variable> variablesOfInterest) {

        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(potentials);

        // Classify unionVariables in two possibles arrays
        List<Variable> variablesToKeep = new ArrayList<Variable>();
        List<Variable> variablesToEliminate = new ArrayList<Variable>();
        for (Variable variable : unionVariables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }

        return DiscretePotentialOperations.multiplyAndMarginalize(potentials,
                variablesToKeep,
                variablesToEliminate);
    }

    /**
     * @param potentials
     *            <code>ArrayList</code> of <code>Potential</code>s to multiply.
     * @param variableToEliminate
     *            <code>Variable</code>.
     * @return result <code>Potential</code> multiplied without
     *         <code>variableToEliminate</code>
     */
    public static TablePotential multiplyAndMarginalize(List<TablePotential> potentials,
            Variable variableToEliminate) {
        List<Variable> variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
        variablesToKeep.remove(variableToEliminate);
        return multiplyAndMarginalize(potentials, variablesToKeep, Arrays.asList(variableToEliminate));
    }

    /**
     * @param potential
     *            <code>Potential</code> to marginalize
     * @param variableToEliminate
     *            <code>Variable</code>
     * @return Marginalized potential
     */
    public static TablePotential marginalize(TablePotential potential, Variable variableToEliminate) {
        List<Variable> variablesToKeep = new ArrayList<Variable>(potential.getVariables());
        variablesToKeep.remove(variableToEliminate);
        List<Variable> variablesToEliminate = new ArrayList<Variable>();
        variablesToEliminate.add(variableToEliminate);
        List<TablePotential> potentials = new ArrayList<TablePotential>();
        potentials.add(potential);
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * @param potential
     * @param variablesOfInterest
     */
    public static TablePotential marginalize(TablePotential potential,
            List<Variable> variablesOfInterest) {
        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> variables = potential.getVariables();

        List<Variable> variablesToKeep = new ArrayList<Variable>();
        List<Variable> variablesToEliminate = new ArrayList<Variable>();

        for (Variable variable : variables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }

        List<TablePotential> potentials = new ArrayList<TablePotential>();
        potentials.add(potential);

        return DiscretePotentialOperations.multiplyAndMarginalize(potentials,
                variablesToKeep,
                variablesToEliminate);
    }

    /**
     * @precondition variablesToKeep + variablesToEliminate =
     *               potential.getVariables()
     * @precondition variablesToKeep
     * @param potential
     *            that will be marginalized
     * @param variablesToKeep
     * @param variablesToEliminate
     * @throws PotentialOperationException
     */
    public static Potential marginalize(TablePotential potential,
            List<Variable> variablesToKeep,
            List<Variable> variablesToEliminate) {
        List<TablePotential> potentials = new ArrayList<TablePotential>();
        potentials.add(potential);
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials,
                variablesToKeep,
                variablesToEliminate);
    }

    /**
     * @param potentials
     *            An array of ordered <code>TablePotential</code>s
     * @return constantFactor: The product of the constant potentials (the first
     *         <i>k</i> because the array is ordered by size)
     * @see org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations#getNonConstantPotentials(ArrayList)
     */
    public static double getConstantFactor(List<TablePotential> potentials) {
        double constantFactor = 1.0;
        for (TablePotential potential : potentials) {
            if (potential.values.length == 1) {
                constantFactor *= potential.values[0];
            }
        }
        return constantFactor;
    }

    /**
     * Compute the accumulated offsets of a <code>Potential</code>s array with
     * the order imposed by <code>potentialResult</code>
     * 
     * @param potentials
     *            <code>ArrayList</code> of <code>Potential</code>s.
     * @param potentialResult
     *            <code>TablePotential</code>.
     * @return An array of arrays of integers (<code>int[][]</code>).
     */
    public static int[][] getAccumulatedOffsets(List<TablePotential> potentials,
            TablePotential potentialResult) {

        int numPotentials = potentials.size();
        int[][] accumulatedOffsets = new int[numPotentials][];

        for (int i = 0; i < numPotentials; i++) {
            TablePotential potential = potentials.get(i);
            accumulatedOffsets[i] = potentialResult.getAccumulatedOffsets(
            // potential.getOriginalVariables());
            potential.getVariables());
        }
        return accumulatedOffsets;
    }

    /**
     * Compute the accumulated offsets of a <code>Potential</code>s array with
     * the order imposed by <code>variables</code>
     * 
     * @param potentials
     *            <code>ArrayList</code> of <code>Potential</code>s.
     * @param variables
     * @return An array of arrays of integers (<code>int[][]</code>).
     */
    public static int[][] getAccumulatedOffsets(List<TablePotential> potentials,
            List<Variable> variables) {

        int numPotentials = potentials.size();
        int[][] accumulatedOffsets = new int[numPotentials][];

        for (int i = 0; i < numPotentials; i++) {
            TablePotential potential = potentials.get(i);
            accumulatedOffsets[i] = TablePotential.getAccumulatedOffsets(variables,
                    potential.getVariables());
        }
        return accumulatedOffsets;
    }

    /**
     * @param potentials
     * @param variablesToEliminate
     * @throws PotentialOperationException
     */
    public static Potential multiplyAndEliminate(List<TablePotential> potentials,
            List<Variable> variablesToEliminate) {

        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
        variablesToKeep.removeAll(variablesToEliminate);

        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * @param potential
     *            a <code>TablePotential</code>
     * @return The <code>potential</code> normalized
     */
    public static TablePotential normalize(TablePotential potential)
            throws NormalizeNullVectorException {
        TablePotential tablePotential = (TablePotential) potential;
        // Check for null vectors
        int p = 0;
        for (p = 0; p < tablePotential.values.length; p++) {
            if (tablePotential.values[p] != 0.0) {
                break;
            }
        }
        if (p == tablePotential.values.length) {
            // All elements in tablePotential.table == 0
            throw new NormalizeNullVectorException("NormalizeNullVectorException: "
                    + "All elements in the TablePotential "
                    + tablePotential.getVariables()
                    + " table are equal to 0.0");
        }
        List<Variable> variables = tablePotential.getVariables();
        if ((variables != null) && (variables.size() > 0)) {
            if (potential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
                int numStates = variables.get(0).getNumStates();
                double normalizationFactor = 0.0;
                for (int i = 0; i < tablePotential.values.length; i += numStates) {
                    normalizationFactor = 0.0;
                    for (int j = 0; j < numStates; j++) {
                        normalizationFactor += tablePotential.values[i + j];
                    }
                    for (int j = 0; j < numStates; j++) {
                        tablePotential.values[i + j] /= normalizationFactor;
                    }
                }
            } else if (potential.getPotentialRole() == PotentialRole.JOINT_PROBABILITY) {
                double normalizationFactor = 0.0;
                for (int i = 0; i < tablePotential.values.length; i++) {
                    normalizationFactor += tablePotential.values[i];
                }
                for (int i = 0; i < tablePotential.values.length; i++) {
                    tablePotential.values[i] /= normalizationFactor;
                }
            }
        }
        
        if(potential.interventions !=null && potential.interventions.length > 0)
        {
        	tablePotential.interventions = potential.interventions.clone();
        }
        return tablePotential;
    }

    /**
     * Divides two <code>TablePotential</code>s using the accumulated offsets
     * algorithm.
     * 
     * @argCondition numerator and denominator have the same domain (variables)
     * @param numerator
     *            <code>Potential</code>.
     * @param denominator
     *            <code>Potential</code>.
     * @return The quotient: A <code>TablePotential</code> with the union of the
     *         variables of numerator and denominator.
     * @throws DivideByZeroException 
     */
    public static TablePotential divide(Potential numerator, Potential denominator) {
        // Get variables and create quotient potential.
        // Quotient potential variables = numerator potential variables union
        // denominator potential variables
        TablePotential tNumerator = (TablePotential) numerator;
        TablePotential tDenominator = (TablePotential) denominator;
        List<Variable> numeratorVariables = new ArrayList<Variable>(tNumerator.getVariables());
        List<Variable> denominatorVariables = new ArrayList<Variable>(tDenominator.getVariables());
        int numNumeratorVariables = numeratorVariables.size();
        int numDenominatorVariables = denominatorVariables.size();
        denominatorVariables.removeAll(numeratorVariables);
        numeratorVariables.addAll(denominatorVariables);
        List<Variable> quotientVariables = numeratorVariables;
        TablePotential quotient = new TablePotential(quotientVariables,
                PotentialRole.JOINT_PROBABILITY);
        if ((numNumeratorVariables == 0) || (numDenominatorVariables == 0)) {
            return divide(tNumerator,
                    tDenominator,
                    quotient,
                    numNumeratorVariables,
                    numDenominatorVariables);
        }

        int numVariables = quotient.getNumVariables();

        // Gets the tables of each TablePotential
        double[][] tables = new double[2][];
        tables[0] = tNumerator.values;
        tables[1] = tDenominator.values;

        // Gets dimension
        int[] quotientDimension = quotient.getDimensions();

        // Gets offset accumulate
        List<TablePotential> potentials = new ArrayList<TablePotential>();
        potentials.add(tNumerator);
        potentials.add(tDenominator);
        int[][] offsetAccumulate = DiscretePotentialOperations.getAccumulatedOffsets(potentials,
                quotient);

        // Gets coordinate
        int[] quotientCoordinate = initializeCoordinates(numVariables);

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
            tamTable = dimension[numVariables - 1] * offset[numVariables - 1];
        }

        for (int quotientPosition = 0; quotientPosition < tamTable; quotientPosition++) {
            /*
             * increment the result coordinate and find out which variable is to
             * be incremented
             */
            for (int iVariable = 0; iVariable < quotientCoordinate.length; iVariable++) {
                // try by incrementing the current variable (given by iVariable)
                quotientCoordinate[iVariable]++;
                if (quotientCoordinate[iVariable] != quotientDimension[iVariable]) {
                    // we have incremented the right variable
                    incrementedVariable = iVariable;
                    // do not increment other variables;
                    break;
                }
                /*
                 * this variable could not be incremented; we set it to 0 in
                 * resultCoordinate (the next iteration of the for-loop will
                 * increment the next variable)
                 */
                quotientCoordinate[iVariable] = 0;
            }

            // divide
            if (tDenominator.values[potentialsPositions[1]] == 0.0) {
                quotient.values[quotientPosition] = 0.0;
            } else {
                quotient.values[quotientPosition] = tNumerator.values[potentialsPositions[0]]
                        / tDenominator.values[potentialsPositions[1]];
            }
            for (int iPotential = 0; iPotential < 2; iPotential++) {
                // update the current position in each potential table
                potentialsPositions[iPotential] += offsetAccumulate[iPotential][incrementedVariable];
            }
        }

        return quotient;
    }

    /**
     * Divide two potentials when one of them has any variable
     * 
     * @param numerator
     *            <code>TablePotential</code>
     * @param denominator
     *            <code>TablePotential</code>
     * @param quotient
     *            <code>TablePotential</code>
     * @param numNumeratorVariables
     *            <code>int</code>
     * @param numDenominatorVariables
     *            <code>int</code>
     * @return quotient The <code>TablePotential</code> received with its table.
     */
    private static TablePotential divide(TablePotential numerator,
            TablePotential denominator,
            TablePotential quotient,
            int numNumeratorVariables,
            int numDenominatorVariables) {
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
    /**
     * @param numerator
     *            <tt>Potential</tt>
     * @param denominator
     *            <tt>Potential</tt>
     * @throws <tt>IllegalArgumentTypeException</tt> if numerator of denominator
     *         are not <tt>TablePotential</tt>
     * @return The quotient
     * @throws DivideByZeroException 
     */
    public static Potential dividePotentials(Potential numerator, Potential denominator)
            throws IllegalArgumentTypeException, DivideByZeroException {
        // parameter correct type verification before calling right method
        if (!(numerator instanceof TablePotential) || !(denominator instanceof TablePotential)) {
            String errMsg = new String("");
            errMsg = errMsg
                    + "Unsupported operation: "
                    + "divide can only manage potentials of type TablePotential.\n";
            if (numerator == null) {
                errMsg = errMsg + "Numerator = null\n";
            } else {
                if (!(numerator instanceof TablePotential)) {
                    errMsg = errMsg + "Numerator class is " + numerator.getClass().getName() + "\n";
                }
            }
            if (denominator == null) {
                errMsg = errMsg + "Denominator = null\n";
            } else {
                if (!(denominator instanceof TablePotential)) {
                    errMsg = errMsg
                            + "Denominator class is "
                            + denominator.getClass().getName()
                            + "\n";
                }
            }
            throw new IllegalArgumentTypeException(errMsg);
        }

        return DiscretePotentialOperations.divide(numerator, denominator);
    }

    /**
     * @param tablePotentials
     *            <code>ArrayList</code> of <code>TablePotential</code>s.
     * @param fsVariablesToKeep
     *            <code>ArrayList</code> of <code>Variable</code>s.
     * @param fsVariableToMaximize
     *            <code>Variable</code>.
     * @return Two potentials: 1) a <code>Potential</code> resulting of
     *         multiplication and maximization of
     *         <code>variableToMaximize</code> and 2) a
     *         <code>GTablePotential</code> of <code>Choice</code> (same
     *         variables as preceding) with the value choosed for
     *         <code>variableToMaximize</code> in each configuration.
     */
    @SuppressWarnings("unchecked")
    public static Object[] multiplyAndMaximize(List<? extends Potential> tablePotentials,
            List<Variable> fSVariablesToKeep,
            Variable fSVariableToMaximize) {
        List<TablePotential> potentials = (ArrayList<TablePotential>) ((Object) tablePotentials);
        List<Variable> variablesToKeep = (ArrayList<Variable>) ((Object) fSVariablesToKeep);

        PotentialRole role = getRole(tablePotentials);

        TablePotential resultingPotential = new TablePotential(variablesToKeep, role);
        if (role == PotentialRole.UTILITY) {
        	resultingPotential.setUtilityVariable(composeVariable(fSVariablesToKeep));
        }

        GTablePotential gResult = new GTablePotential(variablesToKeep, role);
        int numStates = ((Variable) fSVariableToMaximize).getNumStates();
        int[] statesChoosed;
        Choice choice;

        // Constant potentials are those that do not depend on any variables.
        // The product of all the constant potentials is the constant factor.
        double constantFactor = 1.0;
        // Non constant potentials are proper potentials.
        List<TablePotential> properPotentials = new ArrayList<TablePotential>();
        for (Potential potential : potentials) {
            if (potential.getNumVariables() != 0) {
                properPotentials.add((TablePotential) potential);
            } else {
                constantFactor *= ((TablePotential) potential).values[((TablePotential) potential).getInitialPosition()];
            }
        }

        int numProperPotentials = properPotentials.size();

        if (numProperPotentials == 0) {
            resultingPotential.values[0] = constantFactor;
            return new Object[] { resultingPotential, gResult };
        }

        // variables in the resulting potential
        List<Variable> unionVariables = new ArrayList<Variable>();
        unionVariables.add((Variable) fSVariableToMaximize);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        // current coordinate in the resulting potential
        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        // Defines some arrays for the proper potentials...
        double[][] tables = new double[numProperPotentials][];
        int[] initialPositions = new int[numProperPotentials];
        int[] currentPositions = new int[numProperPotentials];
        int[][] accumulatedOffsets = new int[numProperPotentials][];
        // ... and initializes them
        TablePotential unionPotential = new TablePotential(unionVariables, null);
        for (int i = 0; i < numProperPotentials; i++) {
            TablePotential potential = (TablePotential) properPotentials.get(i);
            tables[i] = potential.values;
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = unionPotential.getAccumulatedOffsets(potential.getVariables());
        }

        // The result size is the product of the dimensions of variables to keep
        int resultSize = resultingPotential.values.length;
        // The elimination size is the product of the dimensions of variables to eliminate
        int eliminationSize = 1;
        eliminationSize *= ((Variable) fSVariableToMaximize).getNumStates();

        // Auxiliary variables for the nested loops
        double multiplicationResult; // product of the table values
        double maxValue; // in general, the sum or the maximum
        int increasedVariable = 0; // when computing the next configuration
        
        // outer iterations correspond to the variables to keep
        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
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
            maxValue = multiplicationResult;
            choice.setValue(0); // because in first iteration we have a maximum

            // next inner iterations
            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {

                // find the next configuration and the index of the
                // increased variable
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);
               
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                // multiply the table values of the potentials
                multiplicationResult = constantFactor;
                for (int i = 0; i < numProperPotentials; i++) {
                    multiplicationResult = multiplicationResult * tables[i][currentPositions[i]];
                }
                
                // update the accumulator (for this inner iteration)
                if (multiplicationResult > (maxValue + maxRoundErrorAllowed)) {
                    choice.setValue(innerIteration);
                    maxValue = multiplicationResult;
                } else {
                    if ((multiplicationResult < (maxValue + maxRoundErrorAllowed))
                            && (multiplicationResult >= (maxValue - maxRoundErrorAllowed))) {
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
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);
                
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultingPotential.values[outerIteration] = maxValue;
            gResult.elementTable.add(choice);

        } // end of outer iteration

//        createInterventions(resultingPotential, gResult, fSVariableToMaximize);
        Object[] resultPotentials = { resultingPotential, gResult };
        return resultPotentials;
    }

	/**
     * @param arrayListPotentials
     * @return true if there is utility potential in a list of potentials
     */
    public static boolean isThereAUtilityPotential(List<TablePotential> arrayListPotentials) {
        boolean isThere = false;
        for (int i = 0; (i < arrayListPotentials.size()) && !isThere; i++) {
            isThere = arrayListPotentials.get(i).getPotentialRole() == PotentialRole.UTILITY;
        }
        return isThere;
    }

    /**
     * @param tablePotentials
     *            <code>ArrayList</code> of <code>TablePotential</code>s.
     * @param fsVariablesToKeep
     *            <code>ArrayList</code> of <code>Variable</code>s.
     * @param fsVariableToMaximize
     *            <code>Variable</code>.
     * @return Two potentials: 1) a <code>Potential</code> resulting of
     *         multiplication and maximization of
     *         <code>variableToMaximize</code> and 2) a
     *         <code>TablePotential</code> with the mass probability 1.0
     *         uniformly distributed among the maximizing states of
     *         <code>variableToMaximize</code> in each configuration; this is
     *         typically a policy of a decision.
     */
    public static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> tablePotentials,
            List<Variable> variablesToKeep,
            Variable variableToMaximize) {
        List<TablePotential> potentials = tablePotentials;

        PotentialRole roleResult = (isThereAUtilityPotential(tablePotentials)) ? PotentialRole.UTILITY
                : PotentialRole.CONDITIONAL_PROBABILITY;

        TablePotential resultingPotential = new TablePotential(variablesToKeep, roleResult);

        List<Variable> variablesPolicy = new ArrayList<Variable>();
        variablesPolicy.add(variableToMaximize);
        variablesPolicy.addAll(variablesToKeep);

        TablePotential policy = new TablePotential(variablesPolicy,
                PotentialRole.CONDITIONAL_PROBABILITY);

        // Constant potentials are those that do not depend on any variables.
        // The product of all the constant potentials is the constant factor.
        double constantFactor = 1.0;
        // Non constant potentials are proper potentials.
        List<TablePotential> properPotentials = new ArrayList<TablePotential>();
        for (Potential potential : potentials) {
            if (potential.getNumVariables() != 0) {
                properPotentials.add((TablePotential) potential);
            } else {
                constantFactor *= ((TablePotential) potential).values[((TablePotential) potential).getInitialPosition()];
            }
        }

        int numProperPotentials = properPotentials.size();

        if (numProperPotentials == 0) {
            resultingPotential.values[0] = constantFactor;
            return new TablePotential[] { resultingPotential, policy };
        }
        
        // variables in the resulting potential
        List<Variable> unionVariables = new ArrayList<Variable>();
        unionVariables.add((Variable) variableToMaximize);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        // current coordinate in the resulting potential
        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        // Defines some arrays for the proper potentials...
        double[][] tables = new double[numProperPotentials][];
        int[] initialPositions = new int[numProperPotentials];
        int[] currentPositions = new int[numProperPotentials];
        int[][] accumulatedOffsets = new int[numProperPotentials][];
        // ... and initializes them
        TablePotential unionPotential = new TablePotential(unionVariables, null);
        for (int i = 0; i < numProperPotentials; i++) {
            TablePotential potential = (TablePotential) properPotentials.get(i);
            tables[i] = potential.values;
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = unionPotential
            // .getAccumulatedOffsets(potential.getOriginalVariables());
            .getAccumulatedOffsets(potential.getVariables());
        }

        // The result size is the product of the dimensions of the
        // variables to keeep
        int resultSize = resultingPotential.values.length;
        // The elimination size is the product of the dimensions of the
        // variables to eliminate
        int eliminationSize = 1;
        eliminationSize *= ((Variable) variableToMaximize).getNumStates();

        // Auxiliary variables for the nested loops
        double multiplicationResult; // product of the table values
        double accumulator; // in general, the sum or the maximum
        int increasedVariable = 0; // when computing the next configuration

        List<Integer> statesTies;
        
        // outer iterations correspond to the variables to keep
        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            // Inner iterations correspond to the variables to eliminate
            // accumulator summarizes the result of all inner iterations

            // first inner iteration
            multiplicationResult = constantFactor;
            for (int i = 0; i < numProperPotentials; i++) {
                // multiply the numbers
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            statesTies = new ArrayList<Integer>();
            statesTies.add(0);
            accumulator = multiplicationResult;
            int[] positionTies = new int[eliminationSize];
            int numTies = 0;
            positionTies[numTies] = currentPositions[0];
            numTies++;

            // next inner iterations
            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {

                // find the next configuration and the index of the
                // increased variable
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);
                
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                // multiply the table values of the potentials
                multiplicationResult = constantFactor;
                for (int i = 0; i < numProperPotentials; i++) {
                    multiplicationResult = multiplicationResult * tables[i][currentPositions[i]];
                }

                // update the accumulator (for this inner iteration)
                Double diffWithAccumulator = multiplicationResult - accumulator;
                if (diffWithAccumulator > maxRoundErrorAllowed) {
                    statesTies = new ArrayList<Integer>();
                    statesTies.add(innerIteration);
                    accumulator = multiplicationResult;
                    numTies = 0;
                    positionTies[numTies] = currentPositions[0];
                    numTies++;
                } else {
                    if (Math.abs(diffWithAccumulator) < maxRoundErrorAllowed) {
                        statesTies.add(innerIteration);
                        positionTies[numTies] = currentPositions[0];
                        numTies++;
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
            	increasedVariable = findNextConfigurationAndIndexIncreasedVariable(unionDimensions,
            			unionCoordinate,increasedVariable);
                
                // update the positions of the potentials we are multiplying
                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultingPotential.values[outerIteration] = accumulator;
            assignProbUniformlyInTies(policy,
                    variableToMaximize.getNumStates(),
                    statesTies,
                    resultingPotential.getConfiguration(outerIteration));
        } // end of outer iteration

        TablePotential[] resultPotentials = { resultingPotential, policy };
        return resultPotentials;
    }

    private static void assignProbUniformlyInTies(TablePotential tp,
            int numStatesVariable,
            List<Integer> statesTies,
            int[] policyDomainConfiguration) {
        Double probTies;

        int numStatesTies = statesTies.size();
        probTies = 1.0 / numStatesTies;

        int lenghtPolicyDomainConfiguration = policyDomainConfiguration.length;
        int[] tPConfiguration = new int[lenghtPolicyDomainConfiguration + 1];
        for (int j = 0; j < lenghtPolicyDomainConfiguration; j++) {
            tPConfiguration[j + 1] = policyDomainConfiguration[j];
        }
        // Assign probabilities to states in tie and the other ones
        for (int i = 0; i < numStatesVariable; i++) {
            tPConfiguration[0] = i;
            int posTPConfiguration = tp.getPosition(tPConfiguration);
            double iProb = (statesTies.contains(i)) ? probTies : 0.0;
            tp.values[posTPConfiguration] = iProb;
        }

    }

    /**
     * @param potentialsVariable
     *            <code>ArrayList</code> of <code>Potential</code>s to multiply.
     * @param variableToMaximize
     *            <code>Variable</code>.
     * @return Two potentials: 1) a <code>Potential</code> resulting of
     *         multiplication and maximization of
     *         <code>variableToMaximize</code> and 2) a
     *         <code>GTablePotential</code> of <code>Choice</code> (same
     *         variables as preceding) with the value chosen for
     *         <code>variableToMaximize</code> in each configuration.
     */
    public static Object[] multiplyAndMaximize(List<? extends Potential> potentialsVariable,
            Variable variableToMaximize) {
        // Use a HashSet to add the variables to avoid adding one variable more
        // than one time
        HashSet<Variable> addedVariables = new HashSet<Variable>();
        for (Potential potential : potentialsVariable) {
            addedVariables.addAll(potential.getVariables());
        }
        List<Variable> variablesToKeep = new ArrayList<Variable>(addedVariables);
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximize(potentialsVariable, variablesToKeep, variableToMaximize);
    }

    /**
     * @param potentialsVariable
     *            <code>ArrayList</code> of <code>Potential</code>s.
     * @param fsVariableToMaximize
     *            <code>Variable</code>.
     * @return Two potentials: 1) a <code>Potential</code> resulting of
     *         multiplication and maximization of
     *         <code>variableToMaximize</code> and 2) a
     *         <code>TablePotential</code> with the mass probability 1.0
     *         uniformly distributed among the maximizing states of
     *         <code>variableToMaximize</code> in each configuration; this is
     *         typically a policy of a decision.
     */
    public static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> potentialsVariable,
            Variable variableToMaximize) {
        // Use a HashSet to add the variables to avoid adding one variable more
        // than one time
        HashSet<Variable> addedVariables = new HashSet<Variable>();
        for (TablePotential potential : potentialsVariable) {
            addedVariables.addAll(potential.getVariables());
        }
        List<Variable> variablesToKeep = new ArrayList<Variable>(addedVariables);
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximizeUniformly(potentialsVariable, variablesToKeep, variableToMaximize);
    }

    /**
     * @param potential
     *            one <code>TablePotential</code>.
     * @param variableToMaximize
     *            <code>Variable</code>.
     * @return Two potentials: 1) a <code>Potential</code> resulting of
     *         multiplication and maximization of
     *         <code>variableToMaximize</code> and 2) a
     *         <code>GTablePotential</code> of <code>Choice</code> (same
     *         variables as preceding) with the value chosen for
     *         <code>variableToMaximize</code> in each configuration.
     */
    public static Object[] maximize(Potential potential, Variable variableToMaximize) {
        List<Potential> potentialsVariable = new ArrayList<Potential>();
        potentialsVariable.add(potential);
        List<Variable> variablesToKeep = new ArrayList<Variable>(potential.getVariables());
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximize(potentialsVariable, variablesToKeep, variableToMaximize);
    }
    
    /**
     * Copy the potential received to another potential with the same variables
     * but with the order received in <code>otherVariables</code>
     * 
     * @param potential
     *            <code>TablePotential</code>
     * @param orderVariables
     *            <code>ArrayList</code> of <code>Variable</code>
     * @return The <code>TablePotential</code> generated
     * @argCondition <code>otherVariables</code> are the same variables than the
     *               variables of <code>potential</code>
     */
    public static TablePotential reorder(TablePotential potential, List<Variable> orderVariables) {
    	boolean hasInterventions = false;
        TablePotential newPotential = new TablePotential(orderVariables,
                potential.getPotentialRole());
        int[] accOffsets = potential.getAccumulatedOffsets(orderVariables);
        int[] potentialPositions = new int[potential.getNumVariables()];
        int[] potentialDimensions = potential.getDimensions();
        double[] valuesOrigPotential = potential.values;
        double[] valuesNewPotential = newPotential.values;
        Intervention[] intervOrigPotential = potential.interventions;
        Intervention[] intervNewPotential = null;
        UncertainValue[] uncertainValues = null;
        UncertainValue[] copyUncertainValues = null;
        if (potential.isUncertain()) {
            uncertainValues = potential.uncertainValues;
            newPotential.uncertainValues = new UncertainValue[potential.uncertainValues.length];
            copyUncertainValues = newPotential.uncertainValues;
        }
        hasInterventions = intervOrigPotential!=null && intervOrigPotential.length > 0;
        if (hasInterventions){
        	int newInterventionsLength = potential.interventions.length;
        	newPotential.interventions = new Intervention[newInterventionsLength];
        	intervNewPotential = newPotential.interventions;
        }

        int copyTablePosition = 0;
        int numVariables = orderVariables.size();
        int incrementedVariable, i;
        for (i = 0; i < valuesOrigPotential.length - 1; i++) {
            valuesNewPotential[copyTablePosition] = valuesOrigPotential[i];
            if (potential.isUncertain()) {
                copyUncertainValues[copyTablePosition] = uncertainValues[i];
            }
            if (hasInterventions){
            	intervNewPotential[copyTablePosition] = intervOrigPotential[i];
            }

            for (incrementedVariable = 0; incrementedVariable < numVariables; incrementedVariable++) {
                potentialPositions[incrementedVariable]++;
                if (potentialPositions[incrementedVariable] == potentialDimensions[incrementedVariable]) {
                    potentialPositions[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            copyTablePosition += accOffsets[incrementedVariable];
        }
        valuesNewPotential[copyTablePosition] = valuesOrigPotential[i];
        if (potential.isUncertain()) {
            copyUncertainValues[copyTablePosition] = uncertainValues[i];
        }
        if (hasInterventions){
        	intervNewPotential[copyTablePosition] = intervOrigPotential[i];
        }
        if (potential.isUtility()) {
            newPotential.setUtilityVariable(potential.getUtilityVariable());
        }
        newPotential.properties = potential.properties;
        return newPotential;
    }

    /**
     * Copy the potential received to another potential with the same variables
     * but with changes in the order of states in one of the variables
     * 
     * @param potential
     *            <code>TablePotential</code>
     * @param variable
     *            <code>VariableList</code> whose order of states has changed
     * @param newOrder
     *            array of <code>State</code>s in the new order
     * @return The <code>TablePotential</code> generated
     * */
    public static TablePotential reorder(TablePotential potential,
            Variable variable,
            State[] newOrder) {
        TablePotential copyPotential = (TablePotential) potential.copy();
        double[] tablePotential = potential.values;
        double[] tableCopyPotential = copyPotential.values;
        UncertainValue[] uncertainValues = null;
        UncertainValue[] copyUncertainValues = null;
        int[] displacements = new int[newOrder.length];
        List<Variable> variables = copyPotential.getVariables();
        int variableIndex = variables.indexOf(variable);
        int offset = copyPotential.getOffsets()[variableIndex];
        State[] oldOrder = variable.getStates();
        for(int i=0; i<newOrder.length; ++i)
        {
            displacements[i] = -1;
            int j=0;
            boolean found = false;
            while(!found)
            {
                if(oldOrder[i] == newOrder[j])
                {
                    displacements[i] = j-i;
                    found = true;
                }
                ++j;
            }
        }
        
        if (potential.isUncertain()) {
            uncertainValues = potential.uncertainValues;
            copyPotential.uncertainValues = new UncertainValue[potential.uncertainValues.length];
            copyUncertainValues = copyPotential.uncertainValues;
        }

        for (int i = 0; i < tablePotential.length; i++) {
            int indexOfState = (i / offset) % variable.getNumStates();
            int newIndex = i + (displacements[indexOfState % variable.getNumStates()] * offset);
            tableCopyPotential[newIndex] = tablePotential[i];
            if (potential.isUncertain()) {
                copyUncertainValues[newIndex] = uncertainValues[i];
            }
        }
        if (potential.isUtility()) {
            copyPotential.setUtilityVariable(potential.getUtilityVariable());
        }
        copyPotential.properties = potential.properties;
        return copyPotential;
    }
    
    
    /**
     * @param potentials
     * @return The maximization of a list of potentials defined over the same variables
     */
    public static TablePotential maximize(Set<TablePotential> potentials){
    	TablePotential result;
    	Set<TablePotential> setPot;
    	
    	if (potentials == null){
    		result = null;
    	}
    	else{
    		int numPotentials = potentials.size();
    		if (numPotentials == 0)
    		{
    			result = null;
    		}
    		else
    		{
    			Iterator<TablePotential> iterPotentials = potentials.iterator();
	    		TablePotential potFirst = potentials.iterator().next();
				List<Variable> variablesFirst = potFirst.getVariables();
	    		setPot = new HashSet<>();
	    		setPot.add(potFirst);
	    		while (iterPotentials.hasNext())
	    		{
	    			setPot.add(reorder(iterPotentials.next(),variablesFirst));
	    		}
	    		int lengthValues = potFirst.values.length;
	    		double newValues[] = new double[lengthValues];
				for (int i=0;i<lengthValues;i++){
	    			double max = Double.NEGATIVE_INFINITY;
	    			for (TablePotential pot:setPot)
	    			{
	    				max = Math.max(pot.values[i], max);
	    			}
	    			newValues[i]=max;
	    		}
				
				result = new TablePotential(variablesFirst,potFirst.getPotentialRole(),newValues);
				if (result.getPotentialRole()==PotentialRole.UTILITY){
					result.setUtilityVariable(potFirst.getUtilityVariable());
				}
    		}
    	}
    	return result;
    }
    
    /**
     * @param chanceVariable. <code>Variable</code>
     * @param potentials. <code>List</code> of <code>TablePotential</code>
     * @return. A <code>List</code> with two <code>TablePotential</code>, 
     * marginal probability and new utility in this order.
     */
    public static List<TablePotential> sumOutVariable(Variable chanceVariable, 
    		List<TablePotential> potentials) {
    	return sumOutVariable(chanceVariable,potentials,false);
    }
    
    /**
     * @param chanceVariable. <code>Variable</code>
     * @param potentials. <code>List</code> of <code>TablePotential</code>
     * @return. A <code>List</code> with two <code>TablePotential</code>, 
     * marginal probability and new utility in this order.
     */
    // TODO Documentar sdagInterventions.
    public static List<TablePotential> sumOutVariable(Variable chanceVariable, 
    		List<TablePotential> potentials,boolean sdagInterventions) {
    	// Get probability and utility potentials
    	List<TablePotential> probPotentials = new ArrayList<TablePotential>();
    	List<TablePotential> utilityPotentials = new ArrayList<TablePotential>();
    	classifyProbAndUtilityPotentials(potentials, probPotentials, utilityPotentials);
    	boolean thereIsUtility = utilityPotentials.size() != 0;
    	List<TablePotential> outputPotentials = new ArrayList<TablePotential>(2);
    	TablePotential marginalProb = null; 

    	if (!thereIsUtility) {
    		marginalProb = multiplyAndMarginalize(probPotentials, chanceVariable);
    	} else {
    		TablePotential inputUtilityPotential = sum(utilityPotentials);
    		List<Variable> inputUtilityVariables = inputUtilityPotential.getVariables();
    		boolean thereAreInterventions = inputUtilityPotential.interventions != null;

    		// build the marginal and conditional probabilities
    		TablePotential joinProb = multiply(probPotentials);
    		marginalProb = marginalize(joinProb, chanceVariable);
    		TablePotential conditionalProb = divide(joinProb, marginalProb);

    		// initialize the output utility potential
    		List<Variable> outputUtilityVariables = marginalProb.getVariables();
    		for (Variable variable : inputUtilityPotential.getVariables()) {
    			if (variable != chanceVariable 
    					&& !outputUtilityVariables.contains(variable)) {
    				outputUtilityVariables.add(variable);
    			}
    		}
    		TablePotential outputUtilityPotential = new TablePotential(outputUtilityVariables, PotentialRole.UTILITY);
    		// TODO Check whether the next line can be removed
    		outputUtilityPotential.setUtilityVariable(inputUtilityPotential.getUtilityVariable());
    		if (thereAreInterventions) {
    			int outputValuesLength = outputUtilityPotential.values.length;
    			outputUtilityPotential.interventions = new Intervention[outputValuesLength];
    		}

    		List<Variable> allVariables = new ArrayList<Variable>(outputUtilityVariables.size() + 1);
    		allVariables.add(chanceVariable);
    		allVariables.addAll(outputUtilityVariables);
    		int numVariables = allVariables.size();
    		int[] allVariablesDimensions = TablePotential.calculateDimensions(allVariables); 

    		// constants for the iterations
    		int chanceVariableSize = chanceVariable.getNumStates();
    		int[] accOffsetsConditionalProbPotential = TablePotential.getAccumulatedOffsets(
    				allVariables, conditionalProb.getVariables());
    		int[] accOffsetsInputUtilityPotential = TablePotential.getAccumulatedOffsets(
    				allVariables, inputUtilityVariables);

    		// auxiliary variables that may change in every iteration
    		int[] allVariablesCoordinate = new int[numVariables];
    		int outputUtilityPotentialPosition = 0;
    		int conditionalProbPotentialPosition = 0;
    		int inputUtilityPotentialPosition = 0;
    		int increasedVariable = 0;

    		double[] probabilities = new double[chanceVariableSize];
    		Intervention[] interventions = new Intervention[chanceVariableSize];

    		// outer iterations correspond to the variables to in the outputUtilityPotential
    		for (int outerIteration = 0; outerIteration < TablePotential.computeTableSize(outputUtilityVariables); 
    				outerIteration++) {
    			double sum = 0;
    			// inner iterations correspond to the chance variable to eliminate
    			for (int innerIteration = 0; innerIteration < chanceVariableSize; innerIteration++) {
    				sum += conditionalProb.values[conditionalProbPotentialPosition]
    						* inputUtilityPotential.values[inputUtilityPotentialPosition];
    				if (thereAreInterventions) {
    					probabilities[innerIteration] = 
    							conditionalProb.values[conditionalProbPotentialPosition];
    					interventions[innerIteration] = 
    							inputUtilityPotential.interventions[inputUtilityPotentialPosition];
    				}

    				// find the next configuration and the index of the increased variable
    				increasedVariable = findNextConfigurationAndIndexIncreasedVariable(allVariablesDimensions, 
    						allVariablesCoordinate,increasedVariable);

    				// Update coordinates
    				conditionalProbPotentialPosition += 
    						accOffsetsConditionalProbPotential[increasedVariable];
    				inputUtilityPotentialPosition +=
    						accOffsetsInputUtilityPotential[increasedVariable];
    			}

    			outputUtilityPotential.values[outputUtilityPotentialPosition] = sum;
    			if (thereAreInterventions) {
    				outputUtilityPotential.interventions[outputUtilityPotentialPosition] = 
    						Intervention.averageOfInterventions(chanceVariable, probabilities, interventions,sdagInterventions);
    			}

    			outputUtilityPotentialPosition++;

    		} //end of outer loop

    		// Return the utility potential if some of its values is different from 0.0
    		// or if there are interventions
    		if (thereAreInterventions || thereAreRelevantUtilities(outputUtilityPotential)) {
    			outputPotentials.add(outputUtilityPotential);
    		}

    	} // end of if (!thereIsUtility)

    	// Do not return the probability potential if it depends on no variables and its value is 1
    	if (marginalProb.getNumVariables() > 0 || !almostEqual(marginalProb.values[0], 1.0)) {
    		marginalProb.setPotentialRole(PotentialRole.JOINT_PROBABILITY);
    		outputPotentials.add(marginalProb);
    	}
    	return outputPotentials;
    }

	/**
	 * @param numVariables
	 * @param dimension
	 * @param coordinate
	 * @param increasedVariable
	 * @return
	 */
	private static int findNextConfigurationAndIndexIncreasedVariable(
			int[] dimension, int[] coordinate, int increasedVariable) {
		boolean isCoordinateJLessThanDimensionJ = false;
		for (int j = 0; j < dimension.length && !isCoordinateJLessThanDimensionJ; j++) {
			coordinate[j]++;
			if (coordinate[j] < dimension[j]){
				increasedVariable = j;
				isCoordinateJLessThanDimensionJ = true;
			}
			else{
				coordinate[j] = 0;
			}
		}
		return increasedVariable;
	}
    
    /**
     * @param decisionVariable. <code>Variable</code>
     * @param probPotentials. <code>List</code> of <code>TablePotential</code>
     * @return. A <code>List</code> with two <code>TablePotential</code>, marginal probability and new utility in this order.
     */
     public static List<TablePotential> maxOutVariable(Variable decisionVariable, 
     		List<TablePotential> probPotentials) {
    	 return maxOutVariable(decisionVariable, probPotentials, false);
     }
    
   /**
    * @param decisionVariable. <code>Variable</code>
    * @param probPotentials. <code>List</code> of <code>TablePotential</code>
    * @param utilityPotentials. <code>List</code> of <code>TablePotential</code>
    * @param sdagInterventions 
    * @return. A <code>List</code> with two <code>TablePotential</code>, marginal probability and new utility in this order.
    */
    public static List<TablePotential> maxOutVariable(Variable decisionVariable, 
    		List<TablePotential> potentials, boolean sdagInterventions) {
    	// Get probability and utility potentials
    	List<TablePotential> probPotentials = new ArrayList<TablePotential>();
    	List<TablePotential> utilityPotentials = new ArrayList<TablePotential>();
    	classifyProbAndUtilityPotentials(potentials, probPotentials, utilityPotentials);
    	List<TablePotential> outputPotentials = new ArrayList<TablePotential>(2);

    	// if there are probability potentials depending on decisionVariable,
    	// its product does not depend on decisionVariable and should be projected
    	boolean thereAreProbabilities = probPotentials.size() > 0;
    	if (thereAreProbabilities) {
    		TablePotential projectedPotential = projectOutVariable(decisionVariable, multiply(probPotentials));
    		if (projectedPotential != null) {
    			outputPotentials.add(projectedPotential);
    		}
    	}

    	TablePotential inputUtilityPotential = sum(utilityPotentials);
    	List<Variable> inputUtilityVariables =  inputUtilityPotential.getVariables();

    	// initialize the output utility potential
    	List<Variable> outputUtilityVariables = inputUtilityPotential.getVariables();
    	outputUtilityVariables.remove(decisionVariable);
    	TablePotential outputUtilityPotential = new TablePotential(outputUtilityVariables, PotentialRole.UTILITY);
    	outputUtilityPotential.interventions = new Intervention[outputUtilityPotential.values.length];

    	// TODO Check whether the next line can be removed
    	outputUtilityPotential.setUtilityVariable(inputUtilityPotential.getUtilityVariable());

    	// in allVariables, the first variable is decisionVariable
    	List<Variable> allVariables = new ArrayList<Variable>(outputUtilityVariables.size() + 1);
    	allVariables.add(decisionVariable);
    	allVariables.addAll(outputUtilityVariables);
    	int numVariables = allVariables.size();
    	int[] allVariablesDimensions = TablePotential.calculateDimensions(allVariables); 

    	// initialize the policy 
    	TablePotential policyPotential = new TablePotential(allVariables, PotentialRole.POLICY);
    	double[] policyValues = policyPotential.values;

    	// constants for the iterations
    	int decisionVariableSize = decisionVariable.getNumStates();
    	int[] accOffsetsInputUtilityPotential = TablePotential.getAccumulatedOffsets(
    			allVariables, inputUtilityVariables);

    	// auxiliary variables that may change in every iteration
    	int[] allVariablesCoordinate = new int[numVariables];
    	int outputUtilityPotentialPosition = 0;
    	int inputUtilityPotentialPosition = 0;
    	int increasedVariable = 0;

    	double max;
    	ArrayList<Integer> optimalStatesIndices = new ArrayList<Integer>(decisionVariableSize);

    	double[] utilities = new double[decisionVariableSize];
    	Intervention[] interventions = new Intervention[decisionVariableSize];

    	// outer iterations correspond to the variables to in the outputUtilityPotential
    	for (int outerIteration = 0; 
    			outerIteration < TablePotential.computeTableSize(outputUtilityVariables); 
    			outerIteration++) {
    		// reset auxiliary variables before enteriong the loop
    		max = Double.NEGATIVE_INFINITY;
    		optimalStatesIndices.clear();
    		// inner iterations correspond to the decision variable to eliminate
    		for (int innerIteration = 0; innerIteration < decisionVariableSize; 
    				innerIteration++) {
    			double auxInputUtilityPotentialValue = inputUtilityPotential.values[inputUtilityPotentialPosition];
    			if (auxInputUtilityPotentialValue >= max){
    				if (auxInputUtilityPotentialValue > max) {
    					max = auxInputUtilityPotentialValue;
    					optimalStatesIndices.clear();
    				}
    				optimalStatesIndices.add(innerIteration);
     			}
    			utilities[innerIteration] = auxInputUtilityPotentialValue;
    			if (inputUtilityPotential.interventions != null) {
    				interventions[innerIteration] = 
    						inputUtilityPotential.interventions[inputUtilityPotentialPosition];
    			}

    			// find the next configuration and the index of the increased variable
    			increasedVariable = findNextConfigurationAndIndexIncreasedVariable(allVariablesDimensions,
    					allVariablesCoordinate,increasedVariable);
    			
    			// Update coordinates
    			inputUtilityPotentialPosition +=
    					accOffsetsInputUtilityPotential[increasedVariable];
    		}

    		outputUtilityPotential.values[outputUtilityPotentialPosition] = max;
    		//TODO In testing phase it is easier to assume that there are no ties between interventions
    		outputUtilityPotential.interventions[outputUtilityPotentialPosition] = 
    				Intervention.optimalInterventionTakingAllOptimal(decisionVariable, utilities, interventions,sdagInterventions);

    		// set the values of policyPotential
    		int policyPotentialPosition = outputUtilityPotentialPosition * decisionVariableSize;
			double probForOptimalStates = 1.0 / optimalStatesIndices.size();
			for (int i = 0; i < decisionVariableSize; i++) {
				policyValues[policyPotentialPosition + i] = (optimalStatesIndices
						.contains(i)) ? probForOptimalStates : 0.0;
			}

    		outputUtilityPotentialPosition++;

    	} // end of the outer loop

    	// Return the utility potential if some of its values is different from 0.0
    	// or if any of the interventions is not null
		if (thereAreInterventionsInOutputUtilityPotential(outputUtilityPotential)
				|| thereAreRelevantUtilities(outputUtilityPotential)) {
			outputPotentials.add(outputUtilityPotential);
		}

    	outputPotentials.add(policyPotential);

    	return outputPotentials;
    }

    /**
     * @param outputUtilityPotential
     * @return boolean
     */
    private static boolean thereAreRelevantUtilities(TablePotential outputUtilityPotential) {
    	boolean thereAreRelevantUtilities = false;
    	for (int i = 0; i < outputUtilityPotential.values.length; i++) {
    		if (!almostEqual(outputUtilityPotential.values[i], 0.0)) {
    			thereAreRelevantUtilities = true;
    			break;
    		}
    	}
    	return thereAreRelevantUtilities;
    }

    /**
     * @param outputUtilityPotential
     * @return boolean
     */
    private static boolean thereAreInterventionsInOutputUtilityPotential(TablePotential outputUtilityPotential) {

    	boolean thereAreInterventions = false;
    	for (int i = 0; i < outputUtilityPotential.values.length; i++) {
    		if (outputUtilityPotential.interventions[i] != null) {
    			thereAreInterventions = true;
    			break;
    		}
    	}
    	return thereAreInterventions;
    }

    /** This method is used to remove a decision variable from a probability potential
     * that in fact does not depend on the decision variable
     * @param variable. <code>Variable</code>
     * @param potential. <code>List</code> of <code>TablePotential</code>
     * @param utilityPotentials. <code>List</code> of <code>TablePotential</code>
     * @return. A <code>List</code> with two <code>TablePotential</code>, marginal probability and new utility in this order.
     */
    public static TablePotential projectOutVariable(Variable variable, TablePotential inputPotential) {
    	List<Variable> inputPotentialVariables = inputPotential.getVariables();
    	int numInputVariables = inputPotentialVariables.size();

    	// initialize the output potential
    	List<Variable> projectedPotentialVariables = inputPotential.getVariables();
    	projectedPotentialVariables.remove(variable);
    	TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, 
    			PotentialRole.JOINT_PROBABILITY);

    	// in allVariables, the first variable is variable
    	List<Variable> allVariables = new ArrayList<Variable>(projectedPotentialVariables.size() + 1);
    	allVariables.add(variable);
    	allVariables.addAll(projectedPotentialVariables);

    	// constants for the iterations
    	int variableSize = variable.getNumStates();
    	int[] allVariablesDimensions = TablePotential.calculateDimensions(allVariables); 
    	int[] accOffsetsInputPotential = TablePotential.getAccumulatedOffsets(
    			allVariables, inputPotentialVariables);
    	int[] accOffsetsProjectedPotential = TablePotential.getAccumulatedOffsets(
    			allVariables, projectedPotentialVariables); 

    	// auxiliary variables that may change in every iteration
    	int[] allVariablesCoordinate = new int[numInputVariables];
    	int inputPotentialPosition = 0;
    	int projectedPotentialPosition = 0;
    	int increasedVariable = 0;

    	// outer iterations correspond to the variables in the output potential
    	int numOuterIterations = TablePotential.computeTableSize(projectedPotentialVariables);
    	for (int outerIteration = 0; outerIteration < numOuterIterations; outerIteration++) {
    		// inner iterations correspond to the variable to eliminate
    		for (int innerIteration = 0; innerIteration < variableSize; innerIteration++) {
    			projectedPotential.values[projectedPotentialPosition] = inputPotential.values[inputPotentialPosition];

    			if (!(outerIteration == numOuterIterations - 1 && innerIteration == variableSize - 1)) {
    				// find the next configuration and the index of the increased variable
    				increasedVariable = findNextConfigurationAndIndexIncreasedVariable(allVariablesDimensions,
    						allVariablesCoordinate,increasedVariable);

    				// Update coordinates
    				inputPotentialPosition +=
    						accOffsetsInputPotential[increasedVariable];
    				projectedPotentialPosition +=
    						accOffsetsProjectedPotential[increasedVariable];
    			}
    		}
    	} // end of the outer loop

    	// Do not return the probability potential if it depends on no variables and its value is 1
    	boolean thereAreRelevantProbabilities = projectedPotential.getNumVariables() > 0 || !almostEqual(projectedPotential.values[0], 1.0);

    	return thereAreRelevantProbabilities ? projectedPotential : null;
    }


    /** 
     * Classifies potential from the first list between probability and utility and stores them 
     * in the second and third list
     * @param potentials. <code>List</code> of <code>TablePotential</code>
     * @param probPotentials. <code>List</code> of <code>TablePotential</code>
     * @param utilityPotentials. <code>List</code> of <code>TablePotential</code>
     */
    public static void classifyProbAndUtilityPotentials(
    		List<TablePotential> potentials,
    		List<TablePotential> probPotentials,
    		List<TablePotential> utilityPotentials) {
    	for (TablePotential potential : potentials) {
    		if (potential.getPotentialRole() == PotentialRole.UTILITY) {
    			utilityPotentials.add(potential);
    		} else {
    			probPotentials.add(potential);
    		}
    	}
    }


    /** 
     * Compares two numbers
     * @param a. <code>double</double>
     * @param b. <code>double</double>
     * @return <code>true</code> when a and b are close.
     */
    private static boolean almostEqual(double a, double b) {
    	double aux = b - a;
    	return (aux >= 0.0 && aux <= maxRoundErrorAllowed) || 
    			(aux <= 0.0 && aux >= -maxRoundErrorAllowed);
    }

}
