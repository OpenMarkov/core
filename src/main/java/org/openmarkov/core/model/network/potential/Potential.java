/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author marias
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public abstract class Potential {
	// Constants
	/**
	 * Maximum size of a String used in toString()
	 */
	protected static final int STRING_MAX_LENGTH = 300;
	// Attributes
	/**
	 * This object contains all the information that the parser reads from disk
	 * that does not have a direct connection with the attributes stored in the
	 * <code>Potential</code> object.
	 */
	public Map<String, Object> properties;
	//    /**
	//     * Utility variable associated to the <code>Node</code> that contains
	//     * this potential.
	//     */
	//    protected Variable             utilityVariable;
	/**
	 * <code>List</code> of <code>Variable</code>s.
	 *
	 * @frozen
	 */
	protected List<Variable> variables;
	/**
	 * Decision criterion. It is used only during inference. In edition,
	 * the node/variable has a criterion, but the potential does not.
	 */
	protected Criterion criterion;
	/**
	 * @frozen
	 */
	protected PotentialRole role;
	protected String comment = "";

	// Constructor

	/**
	 * @param variables <code>ArrayList</code> of <code>Variable</code>.
	 * @param role      <code>PotentialRole</code>
	 */
	public Potential(List<Variable> variables, PotentialRole role) {
		if (variables != null) {
			this.variables = new ArrayList<>(variables);
		} else {
			this.variables = new ArrayList<>();
		}
		properties = new HashMap<>();
		this.role = role;
	}

	//    /**
	//     * @param variables <code>List</code> of <code>Variable</code>s.
	//     * @param utilityVariable
	//     */
	//    public Potential (Variable utilityVariable, List<Variable> variables)
	//    {
	//        this(variables, PotentialRole.UTILITY);
	//        this.utilityVariable = utilityVariable;
	//    }

	/**
	 * TODO - Remove this constructor, replace with a copy method
	 * Copy constructor for potential
	 *
	 * @param potential
	 */
	public Potential(Potential potential) {
		this(potential.getVariables(), potential.getPotentialRole());
		this.comment = potential.getComment();
	}

	// Methods

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>.
	 * @param role      <code>PotentialRole</code>.
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		// Default implementation: always return true
		return true;
	}

	protected static List<Variable> toList(Variable[] variables) {
		List<Variable> variablesArrayList = new ArrayList<>();
		for (Variable variable : variables) {
			variablesArrayList.add(variable);
		}
		return variablesArrayList;
	}

	protected static TablePotential findPotentialByVariable(Variable variable, List<TablePotential> potentials) {
		int i = 0;
		TablePotential potential = null;
		while (i < potentials.size() && potential == null) {
			if (variable.equals(potentials.get(i).getConditionedVariable())) {
				potential = potentials.get(i);
			}
			++i;
		}
		return potential;
	}

	/**
	 * @param evidenceCase <code>EvidenceCase</code>
	 * @return The conditional probability table of this potential given the
	 * evidence
	 * @throws WrongCriterionException
	 * @throws NonProjectablePotentialException
	 */
	public TablePotential getCPT(EvidenceCase evidenceCase)
			throws NonProjectablePotentialException, WrongCriterionException {
		List<TablePotential> potentials = tableProject(evidenceCase, null);
		HashSet<Variable> variablesToEliminate = new HashSet<>();
		// Fill it with variables appearing in all potentials except this
		for (TablePotential tablePotential : potentials) {
			variablesToEliminate.addAll(tablePotential.getVariables());
		}
		variablesToEliminate.removeAll(variables);
		return DiscretePotentialOperations
				.multiplyAndMarginalize(potentials, variables, new ArrayList<>(variablesToEliminate));
	}

	/**
	 * The conditional probability table given by this potential
	 *
	 * @return <code>TablePotential</code>
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	public TablePotential getCPT() throws NonProjectablePotentialException, WrongCriterionException {
		return getCPT(new EvidenceCase());
	}

	/**
	 * Checks if all the variables belongs to the type received. The utility
	 * variable is not considered.
	 *
	 * @return <code>boolean</code>
	 */
	protected boolean noNumericVariables() {
		if (variables != null) {
			for (Variable variable : variables) {
				if (variable.getVariableType() == VariableType.NUMERIC) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return A <code>List</code> of <code>Variable</code>s
	 * @consultation
	 */
	public List<Variable> getVariables() {
		return new ArrayList<>(variables);
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	/**
	 * @return The variable in the place <code>position</code>
	 * @consultation
	 */
	public Variable getVariable(int position) {
		return variables.get(position);
	}

	public void replaceVariable(Variable variableToReplace, Variable variable) {
		// TODO - Check if OOPN and ConditionalGaussian potential are still running
		//        if (variableToReplace.equals (utilityVariable))
		//        {
		//        	utilityVariable = variable;
		//        }
		//        else

		if (variables.contains(variableToReplace)) {
			replaceVariable(variables.indexOf(variableToReplace), variable);
		}
	}

	// TODO documentar

	public void replaceVariable(int position, Variable variable) {
		variables.remove(position);
		variables.add(position, variable);
	}

	/**
	 * @param variable <code>Variable</code>
	 * @return <code>true</code> if contains the received <code>Variable</code>.
	 */
	public boolean contains(Variable variable) {
		return variables.contains(variable);
	}

	/**
	 * @param evidenceCase               <code>EvidenceCase</code>
	 * @param inferenceOptions
	 * @param alreadyProjectedPotentials <code>List</code> of already projected potentials
	 * @throws WrongCriterionException
	 * @throws NoFindingException
	 */
	public abstract List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
			List<TablePotential> alreadyProjectedPotentials)
			throws NonProjectablePotentialException, WrongCriterionException;

	//    /** @return isUtility <code>boolean</code> */
	//    public boolean isUtility ()
	//    {
	//        return role == PotentialRole.UTILITY;
	//    }

	public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
			throws NonProjectablePotentialException, WrongCriterionException {
		return tableProject(evidenceCase, inferenceOptions, new ArrayList<TablePotential>());
	}

	public Potential project(EvidenceCase evidenceCase)
			throws WrongCriterionException, NonProjectablePotentialException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return isAdditive <code>boolean</code>
	 * Whether the potential is additive in the inference. Only the potentials that are in a Markov network
	 * and were associated to a utility node/variable in the original network have a criterion and we must maximize them.
	 * This characterization of "utility potentials" is relevant only during inference.
	 */
	public boolean isAdditive() {
		return criterion != null;
	}

	/**
	 * @return number of variables: <code>int</code>
	 */
	public int getNumVariables() {
		return variables.size();
	}

	public Variable getConditionedVariable() {
		return variables.isEmpty() ? null : variables.get(0);
	}

	/**
	 * Generates new <code>Finding</code>s generated by an
	 * <code>EvidenceCase</code>. In principle this method does not generate any
	 * new finding, but it is overridden in some of its subclasses.
	 *
	 * @param evidenceCase <code>EvidenceCase</code>
	 * @return <code>Collection</code> of <code>Finding</code>s
	 * @throws IncompatibleEvidenceException
	 * @throws WrongCriterionException
	 */
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase)
			throws IncompatibleEvidenceException, WrongCriterionException {
		return new ArrayList<>();
	}

	/**
	 * @return role. <code>PotentialRole</code>
	 */
	public PotentialRole getPotentialRole() {
		return role;
	}

	/**
	 * Modifies the frozen variable role. This method exists to avoid some
	 * problems with legacy code in DiscretePotentialOperations class and it
	 * does not be used except in very special cases.
	 *
	 * @param role <code>PotentialRole</code>
	 */
	public void setPotentialRole(PotentialRole role) {
		this.role = role;
	}

	/**
	 * @return comment. <code>String</code>
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment <code>String</code>
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Shifts the potential in time as indicated by <code>timeDifference</code>.<p>
	 * Subclasses of Potential must override this method.
	 *
	 * @param timeDifference <code>int</code>
	 * @param probNet        This parameter is necessary because the shifted variables
	 *                       are taken from the network. <code>ProbNet</code>
	 * @return <code>Potential</code>
	 * @throws NodeNotFoundException
	 */
	public void shift(ProbNet probNet, int timeDifference) throws NodeNotFoundException {
		setVariables(getShiftedVariables(probNet, timeDifference));
	}

	/**
	 * Creates links between the first variable of a potential and the rest of the variables.
	 *
	 * @argCondition The role of the potential must be utility of conditional
	 * probability
	 * @argCondition The network must contain all the variables of the potential
	 */
	public void createDirectedLinks(ProbNet probNet) {
		int numVariables = variables.size();
		if (numVariables > 1) {
			Variable childVariable = variables.get(0);
			for (int i = 1; i < numVariables; i++) {
				try {
					probNet.addLink(variables.get(i), childVariable, true);
				} catch (NodeNotFoundException e) {
					// Unreachable code
					System.err.println("Reached unreachable code in Potential.createDirectedLinks: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Returns a list with the same variables as this potential, including the
	 * utility variable but shifted in time as indicated by timeDifference
	 *
	 * @throws NodeNotFoundException
	 * @argCondition The network must contain the shifted variables.
	 */
	public List<Variable> getShiftedVariables(ProbNet probNet, int timeDifference) throws NodeNotFoundException {
		List<Variable> shiftedVariables = new ArrayList<Variable>(variables.size());

		// also shift variables within the tree
		for (Variable variable : variables) {
			if (variable.isTemporal()) {
				shiftedVariables.add(probNet.getShiftedVariable(variable, timeDifference));
			} else {
				shiftedVariables.add(variable);
			}
		}

		return shiftedVariables;
	}

	/**
	 * Overrides <code>toString</code> method. Mainly for test purposes
	 */
	public String toString() {
		return toShortString();
	}

	public String toShortString() {
		StringBuilder buffer = new StringBuilder();
		int numVariables = (variables != null) ? variables.size() : 0;
		if (numVariables == 0) { // Constant potential
			switch (role) {
			case CONDITIONAL_PROBABILITY:
				break;
			case JOINT_PROBABILITY:
				break;
			default:
				break;
			}
		} else {
			switch (role) {
			case CONDITIONAL_PROBABILITY:
				buffer.append("P(" + variables.get(0));
				if (numVariables > 1) {
					buffer.append(" | ");
					printVariables(buffer, 1);
				}
				buffer.append(")");
				break;
			case JOINT_PROBABILITY:
				buffer.append("P(");
				printVariables(buffer, 0);
				buffer.append(")");
				break;
			default:
				buffer.append(numVariables + " Variables: ");
				if (numVariables > 0) {
					buffer.append(variables.get(0).getName());
					for (int i = 1; i < numVariables - 1; i++) {
						buffer.append(", " + variables.get(i).getName());
					}
					if (numVariables > 1) {
						buffer.append(", " + variables.get(numVariables - 1).getName());
					}
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * Prints in buffer the variables and in case of TablePotential the
	 * configurations
	 */
	private StringBuilder printVariables(StringBuilder buffer, int firstVariable) {
		// Print variables
		for (int i = firstVariable; i < variables.size() - 1; i++) {
			buffer.append(variables.get(i) + ", ");
		}
		buffer.append(variables.get(variables.size() - 1));
		return buffer;
	}

	public String treeADDString() {
		return toString();
	}

	/**
	 * @returns a sampled potential. By default, itself, i.e., not sampled.
	 * TODO This method must be commented further
	 */
	public Potential sample() {
		return this; // By default
	}

	@Override public boolean equals(Object arg0) {
		if (arg0.getClass().equals(this.getClass())) {
			Potential potential = (Potential) arg0;
			return variables.equals(potential.variables) && role == potential.role;
		} else {
			return false;
		}
	}

	/**
	 * When this potential represents a conditional probability, this method returns a value for the first variable,
	 * sampled with the probability distribution. If this variable is finite-states, it returns the index of
	 * the sampled state. If the variable is numeric, it returns the value sampled.
	 * @param randomGenerator
	 * @param sampledParents
	 * @return
	 */
	// TODO replace int with double
	// TODO make this method abstract and implement it in all the subclasses of Potential
	public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> sampledParents) {
		// dummy code. TODO remove when making this method abstract.
		return Integer.MAX_VALUE;
	}

	/**
	 * Return a copy instance of the potential
	 *
	 * @return potential copy
	 */
	public abstract Potential copy();

	/**
	 * Return true if potential has uncertainty values
	 *
	 * @return whether the potential has uncertainty or not
	 */
	public abstract boolean isUncertain();

	/**
	 * Adds variable to a potential implemented in each child class
	 */
	public Potential addVariable(Variable variable) {
		if (!variables.contains(variable)) {
			variables.add(variable);
		}
		Potential newPotential = new UniformPotential(variables, role);
		return newPotential;
	}

	/**
	 * Removes variable to a potential implemented in each child class
	 */
	public Potential removeVariable(Variable variable) {
		variables.remove(variable);
		Potential newPotential = new UniformPotential(variables, role);
		return newPotential;
	}

	public double getProbability(HashMap<Variable, Integer> sampledStateIndexes) {
		return 0;
	}

	public double getProbability(EvidenceCase evidenceCase) {
		HashMap<Variable, Integer> configuration = new HashMap<>();
		for (Finding finding : evidenceCase.getFindings()) {
			configuration.put(finding.getVariable(), finding.getStateIndex());
		}
		return getProbability(configuration);
	}

	public void replaceNumericVariable(Variable convertedParentVariable) {
		int varIndex = -1;
		for (int i = 0; i < variables.size(); ++i) {
			if (variables.get(i).getName().equals(convertedParentVariable.getName())) {
				varIndex = i;
			}
		}
		if (varIndex != -1) {
			variables.set(varIndex, convertedParentVariable);
		}
	}

	/**
	 * Multiply the potential by a scale. If the Potential is not scalable it must throw UnsupportedOperationException
	 *
	 * @param scale
	 */
	public abstract void scalePotential(double scale);

	/**
	 * Copy this potential attributes to the newPotential potential of the copyNet
	 *
	 * @param copyNet
	 * @return
	 */
	public Potential deepCopy(ProbNet copyNet) {
		Potential potential = newInstance();

		List<Variable> newReferences = new ArrayList<>();
		for (Variable variable : this.variables) {
			try {
				newReferences.add(copyNet.getVariable(variable.getName()));
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
		}

		potential.setVariables(newReferences);
		potential.setPotentialRole(this.getPotentialRole());
		potential.setComment(new String(this.comment));

		return potential;
	}

	private Potential newInstance() {
		try {
			//this creates an instance of the subclass
			return this.getClass().getConstructor(this.getClass()).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Criterion getCriterion() {
		return criterion;
	}

	public void setCriterion(Criterion criterion) {
		this.criterion = criterion;
	}
}
