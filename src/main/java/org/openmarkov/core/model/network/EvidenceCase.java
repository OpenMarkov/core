/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class stores an <code>ArrayList</code> of <code>Findings</code> and can
 * search them with the name.
 *
 * @author marias
 * @author fjdiez
 * @version 1.0
 * @see org.openmarkov.core.model.network.Finding
 * @since OpenMarkov 1.0
 */
public class EvidenceCase {

	// Attribute
	/**
	 * List of findings <code>HashMap</code> of key=<code>String</code> and
	 * value=<code>Finding</code>.
	 */
	protected HashMap<Variable, Finding> findings;

	// Constructors

	/**
	 * @param findings <code>HashMap</code> of key=<code>String</code> and value=
	 *                 <code>Finding</code>.
	 */
	public EvidenceCase(HashMap<Variable, Finding> findings) {
		this.findings = findings;
	}

	/**
	 * Constructor
	 *
	 * @param findings
	 */
	public EvidenceCase(List<Finding> findings) {
		this.findings = new HashMap<>();
		for (Finding finding : findings) {
			this.findings.put(finding.getVariable(), finding);
		}
	}

	public EvidenceCase() {
		findings = new HashMap<>();
	}

	/**
	 * Copy constructor
	 *
	 * @param evidenceCase
	 */
	public EvidenceCase(EvidenceCase evidenceCase) {
		if (evidenceCase == null) {
			findings = new HashMap<>();
		} else {
			findings = new HashMap<>(evidenceCase.findings);
		}
	}

	// Methods

	/**
	 * @return The state assigned to the variable. <code>int</code>.
	 * @throws NoFindingException
	 * @argCondition There is a finding for this variable in the evidence
	 */
	public int getState(Variable variable) {
		return getFinding(variable).getStateIndex();
	}

	/**
	 * @param variable <code>Variable</code>.
	 * @return The value of a evidence for a continuous or hybrid variable if it
	 * exists: <code>double</code>.
	 * @argCondition There is a finding for this variable in the evidence
	 */
	public double getNumericalValue(Variable variable) {
		return getFinding(variable).getNumericalValue();
	}

	/**
	 * @param finding . <code>Finding</code>.
	 * @throws InvalidStateException
	 * @throws IncompatibleEvidenceException
	 */
	public void addFinding(Finding finding) throws InvalidStateException, IncompatibleEvidenceException {
		if (isCompatible(finding)) {
			if (!findings.containsKey(finding.getVariable())) {
				findings.put(finding.getVariable(), finding);
			}
		} else {
			throw new IncompatibleEvidenceException(
					"Error trying to add " + "evidence: " + finding.toString() + " having previously " + "evidence: "
							+ findings.get(finding.getVariable()));
		}
	}

	/**
	 * @param finding . <code>Finding</code>.
	 * @throws InvalidStateException
	 * @throws IncompatibleEvidenceException
	 */
	public void changeFinding(Finding finding) throws InvalidStateException, IncompatibleEvidenceException {
		findings.remove(finding.getVariable());
		addFinding(finding);
	}

	/**
	 * @param findings . <code>Collection</code> of <code>Finding</code>s.
	 * @throws InvalidStateException
	 * @throws IncompatibleEvidenceException
	 */
	public void addFindings(Collection<Finding> findings) throws InvalidStateException, IncompatibleEvidenceException {
		for (Finding finding : findings) {
			addFinding(finding);
		}
	}

	/**
	 * @param probNet
	 * @param variableName
	 * @param stateName    <code>Finding</code>.
	 * @throws InvalidStateException
	 * @throws IncompatibleEvidenceException
	 * @throws NodeNotFoundException
	 */
	public void addFinding(ProbNet probNet, String variableName, String stateName)
			throws NodeNotFoundException, InvalidStateException, IncompatibleEvidenceException {
		Variable variable = probNet.getVariable(variableName);
		int stateIndex = variable.getStateIndex(stateName);
		addFinding(new Finding(variable, stateIndex));
	}

	/**
	 * @param probNet
	 * @param variableName
	 * @param value        <code>Finding</code>.
	 * @throws IncompatibleEvidenceException
	 * @throws NodeNotFoundException
	 */
	public void addFinding(ProbNet probNet, String variableName, double value)
			throws NodeNotFoundException, InvalidStateException, IncompatibleEvidenceException {
		Variable variable = probNet.getVariable(variableName);
		Finding finding = new Finding(variable, value);
		addFinding(finding);
	}

	/**
	 * @param variable <code>Variable</code>.
	 * @throws NoFindingException
	 */
	public Finding removeFinding(Variable variable) throws NoFindingException {
		Finding finding = getFinding(variable);
		if (finding == null) {
			throw new NoFindingException(variable);
		}
		return findings.remove(finding.getVariable());
	}

	/**
	 * @param variableName <code>String</code>.
	 * @throws NoFindingException
	 */
	public void removeFinding(String variableName) throws NoFindingException {
		ArrayList<Variable> findingsVariables = new ArrayList<>(findings.keySet());
		int i = 0, numVariables = findingsVariables.size();
		Variable variable = null;
		do {
			variable = findingsVariables.get(i++);
		} while (i < numVariables && !variable.getName().contentEquals(variableName));
		if (variable == null) {
			throw new NoFindingException(variableName);
		} else {
			findings.remove(variable);
		}
	}

	/**
	 * @return The set of variables associated to the set of findings in the
	 * same order: <code>ArrayList</code> of <code>Variable</code>.
	 */
	public List<Variable> getVariables() {
		return new ArrayList<>(findings.keySet());
	}

	/**
	 * @param variable <code>String</code>.
	 * @return finding <code>Finding</code>.
	 * @argCondition There is a finding for this variable in the evidence
	 */
	public Finding getFinding(Variable variable) {
		return findings.get(variable);
	}

	/**
	 * @return findings: <code>ArrayList</code> of <code>Finding</code>s.
	 */
	public List<Finding> getFindings() {
		return new ArrayList<>(findings.values());
	}

	/**
	 * Returns true if the evidence case contains a finding for this variable.
	 *
	 * @param variable . <code>Variable</code>
	 * @return <code>boolean</code>.
	 */
	public boolean contains(Variable variable) {
		return findings.containsKey(variable);
	}

	/**
	 * @param variables . <code>ArrayList</code> of <code>Variable</code>s.
	 * @return <code>boolean</code>.
	 * @throws NoFindingException
	 */
	public boolean existsEvidence(List<Variable> variables) {
		for (Variable variable : variables) {
			if (findings.get(variable) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Keeps the nodes of the received <code>probNet</code> that has not
	 * received evidence.
	 *
	 * @param probNet <code>ProbNet</code>.
	 * @return An <code>ArrayList</code> of <code>Node</code>s.
	 */
	public List<Node> getRemainingNodes(ProbNet probNet) {
		List<Node> probNetNodes = probNet.getNodes();
		List<Node> remainingNodes = new ArrayList<>();
		for (Node node : probNetNodes) {
			if (!contains(node.getVariable())) {
				remainingNodes.add(node);
			}
		}
		return remainingNodes;
	}

	/**
	 * @return <code>true</code> if there are no findings. <code>boolean</code>
	 */
	public boolean isEmpty() {
		return findings.isEmpty();
	}

	/**
	 * Overrides <code>toString</code> method. Mainly for test purposes. It
	 * writes the name of the variables and the findings.
	 */
	public String toString() {
		String string = new String("[");
		Collection<Finding> findingsCollection = findings.values();
		for (Finding finding : findingsCollection) {
			if (string.compareTo("[") != 0) {
				string = string + ", ";
			}
			string = string + finding.toString();
		}
		string = string + "]\n";
		return string;
	}

	/**
	 * Extends an evidence case by taking into account that the deterministic
	 * potentials of a <code>ProbNet</code> may induce new findings
	 *
	 * @throws InvalidStateException
	 * @throws WrongCriterionException
	 */
	public void extendEvidence(ProbNet probNet)
			throws IncompatibleEvidenceException, InvalidStateException, WrongCriterionException {
		for (Potential potential : probNet.getPotentials()) {
			List<Finding> newFindings = (List<Finding>) potential.getInducedFindings(this);
			for (Finding newFinding : newFindings) {
				findings.put(newFinding.getVariable(), newFinding);
			}
		}
		Queue<Finding> pendingFindings = new LinkedList<>(findings.values());
		while (!pendingFindings.isEmpty()) {
			Finding oldFinding = pendingFindings.poll();
			Variable oldVariable = oldFinding.getVariable();
			List<Potential> potentials = probNet.getPotentials(oldVariable);
			for (Potential potential : potentials) {
				List<Finding> newFindings = (List<Finding>) potential.getInducedFindings(this);
				for (Finding newFinding : newFindings) {
					if (!findings.containsKey(newFinding.getVariable())) {
						findings.put(newFinding.getVariable(), newFinding);
						pendingFindings.add(newFinding);
					}
				}
			}
		}
	}

	/**
	 * Ensures that the <code>newFinding</code> is not inconsistent with the
	 * actual evidence.
	 *
	 * @param newFinding . <code>Finding</code>
	 * @return <code>boolean</code>
	 * @throws InvalidStateException
	 */
	public boolean isCompatible(Finding newFinding) throws InvalidStateException {
		Variable variable = newFinding.getVariable();
		Finding existingFinding = findings.get(variable);
		if (existingFinding == null) {
			return true;
		} else {
			VariableType variableType = variable.getVariableType();
			switch (variableType) {
			case FINITE_STATES:
				return newFinding.stateIndex == existingFinding.stateIndex;
			case NUMERIC:
				return newFinding.numericalValue == existingFinding.numericalValue;
			case DISCRETIZED:
				return (newFinding.stateIndex == existingFinding.stateIndex) || (
						newFinding.numericalValue == existingFinding.numericalValue
				);
			}
		}
		return true;
	}

	public EvidenceCase shiftEvidenceBackwards(int timeDifference, ProbNet probNet) {
		EvidenceCase shiftedEvidence = new EvidenceCase();
		try {
			for (Finding finding : findings.values()) {
				Variable findingVariable = finding.getVariable();
				// generate shifted finding
				if (findingVariable.isTemporal()) {
					if (probNet.containsShiftedVariable(findingVariable, -timeDifference)) {
						Variable shiftedVariable = probNet.getShiftedVariable(findingVariable, -timeDifference);
						Finding shiftedFinding = new Finding(shiftedVariable, finding.stateIndex);
						shiftedFinding.numericalValue = finding.numericalValue;
						shiftedEvidence.addFinding(shiftedFinding);
					}
				} else {
					// add non-temporal findings
					shiftedEvidence.addFinding(finding);
				}
			}
		} catch (Exception e) {
			// Unreachable code
			throw new Error("shifted finding");
		}
		return shiftedEvidence;
	}

	/**
	 * @return The number of findings in the evidence case
	 */
	public int getNumberOfFindings() {
		int num;
		if (findings == null) {
			num = 0;
		} else {
			num = findings.size();
		}
		return num;
	}

	/**
	 * Fuse this EvidenceCase with the input parameter one
	 *
	 * @param evidenceCaseToFuse
	 * @param overwrite          if true the findings in the parameter will overwrite those in
	 *                           this EvidenceCase
	 * @throws IncompatibleEvidenceException
	 */
	public void fuse(EvidenceCase evidenceCaseToFuse, boolean overwrite) throws IncompatibleEvidenceException {
		if (evidenceCaseToFuse != null) {
			for (Finding finding : evidenceCaseToFuse.getFindings()) {
				try {
					if (this.contains(finding.getVariable())) {
						if (overwrite) {
							changeFinding(finding);
						}
					} else {
						this.addFinding(finding);
					}
				} catch (InvalidStateException ignore) {
				}
			}
		}
	}

}