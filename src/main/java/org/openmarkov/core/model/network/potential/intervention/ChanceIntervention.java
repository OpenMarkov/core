package org.openmarkov.core.model.network.potential.intervention;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.CostEffectivenessException;
import org.openmarkov.core.model.network.Variable;

public class ChanceIntervention extends Intervention {

	// Attributes
	private Variable chanceVariable;
	
	private double[] chanceVariableDistribution;
	
	private Intervention[] interventions;

	// Constructor
	/** 
	 * Creates an intervention composed of several sub-interventions in function of a chance variable.
	 * @param decisionVariable. <code>Variable</code>
	 * @param chanceVariable. <code>Variable</code>
	 * @param chanceVariableDistribution. <code>double[]</code>
	 * @param interventions. <code>Intervention[]</code>
	 * @throws CostEffectivenessException
	 */
	public ChanceIntervention(Variable chanceVariable, double[] chanceVariableDistribution, 
			Intervention[] interventions) 
			throws CostEffectivenessException {
		if (chanceVariableDistribution.length != interventions.length ||
				chanceVariableDistribution.length != chanceVariable.getNumStates()) {
			throw new CostEffectivenessException("The chance variable has " + chanceVariable.getNumStates() + 
					" states.\nThere are " + interventions.length + 
					" interventions.\nThe probability distribution has " + 
					chanceVariableDistribution.length + " values.");
		}
		this.chanceVariable = chanceVariable;
		this.chanceVariableDistribution = chanceVariableDistribution;
		this.interventions = interventions;
	}
	
	// Methods
	@Override
	public boolean sameAs(Object object) {
		boolean sameIntervention = false;
		if (object != null && object.getClass() == this.getClass()) {
			ChanceIntervention intervention = (ChanceIntervention)object;
			Intervention[] otherInterventions = intervention.getInterventions();
			double[] otherDistribution = intervention.getChanceVariableDistribution();
			if (!(otherDistribution.length != chanceVariableDistribution.length || (interventions == null && 
					otherInterventions != null) || (interventions != null && otherInterventions == null) || 
					(chanceVariableDistribution == null & otherDistribution != null) || 
					(chanceVariableDistribution != null & otherDistribution == null))) {
				if (interventions != null) {
					int i;
					for (i = 0; i < interventions.length && chanceVariableDistribution[i]==otherDistribution[i] &&
							(chanceVariableDistribution[i] == 0.0 || 
							(chanceVariableDistribution[i] != 0.0 && 
							interventions[i].sameAs(otherInterventions[i]))); i++);
					if (i == interventions.length) {
						sameIntervention = true;
					} 
				}
			}
		}
		else sameIntervention = false;
		return sameIntervention;
	}

	@Override
	protected int getNumBranches() {
		int numBranches = 0;
		boolean[] valuesTakenIntoAccount = new boolean[chanceVariableDistribution.length];
		for (int i = 0; i < chanceVariableDistribution.length; i++) {
			if (chanceVariableDistribution[i] != 0.0 && !valuesTakenIntoAccount[i]) {
				numBranches++;
				for (int j = i + 1; j < chanceVariableDistribution.length; j++) {
					if (chanceVariableDistribution[j] != 0.0 && interventions[i].sameAs(interventions[j])) {
						valuesTakenIntoAccount[j] = true;
					}
				}
			}
		}
		return numBranches;
	}
	
	public String toString() {
		String out = null;
		StringBuffer strBuffer = new StringBuffer();
		if (getNumLeaves() == 1) {
			int i = 0;
			while (chanceVariableDistribution[i++] == 0.0);
			strBuffer.append(indent);
			strBuffer.append(interventions[--i].toString());
		} else {
			Intervention intervention = null;
			boolean allInterventionsAreEqual = true;
			for (int i = 0; i < chanceVariableDistribution.length; i++) {
				if (chanceVariableDistribution[i] != 0.0) {
					if (intervention == null) {
						intervention = interventions[i];
					} else {
						if (allInterventionsAreEqual) {
							allInterventionsAreEqual &= intervention.sameAs(interventions[i]);
						}
					}
				}
			}
			if (allInterventionsAreEqual) {
				if (intervention != null) {
					intervention.setIndentLevel(indentLevel);
					strBuffer.append(intervention.toString());
				}
			} else {
				String ifDec = "If " + chanceVariable.getName() + " = '";
				boolean[] valuesTakenIntoAccount = new boolean[chanceVariableDistribution.length];
				for (int i = 0; i < chanceVariableDistribution.length; i++) {
					if (chanceVariableDistribution[i] != 0.0 && !valuesTakenIntoAccount[i]) {
						strBuffer.append(indent);
						strBuffer.append(ifDec);
						strBuffer.append(chanceVariable.getStateName(i));
						strBuffer.append("'");
						for (int j = i + 1; j < chanceVariableDistribution.length; j++) {
							if (chanceVariableDistribution[j] != 0.0 && interventions[i].sameAs(interventions[j])) {
								strBuffer.append(" OR '");
								strBuffer.append(chanceVariable.getStateName(j));
								strBuffer.append("'");
								valuesTakenIntoAccount[j] = true;
							}
						}
						if (interventions[i].getNumLeaves() > 1) {
							interventions[i].setIndentLevel(indentLevel + indentIncrement);
							strBuffer.append(" then {\n");
							strBuffer.append(interventions[i].toString());
							strBuffer.append(indent);
							strBuffer.append("}\n");
						} else {
							strBuffer.append(" then ");
							strBuffer.append(interventions[i].toString());
						}
					}
				}
			}
		}
		out = strBuffer.toString();
		return out;
	}
	
	/**
	 * @return The sub-interventions. <code>Intervention[]</code>
	 */
	public Intervention[] getInterventions() {
		return interventions;
	}

	/**
	 * @return <code>double[]</code>
	 */
	public double[] getChanceVariableDistribution() {
		return chanceVariableDistribution;
	}
	
	/**
	 * @param index. <code>int</code>
	 * @return <code>double</code>
	 */
	public double getProbability(int index) {
		return chanceVariableDistribution[index];
	}

	/**
	 * @return
	 */
	public Variable getChanceVariable() {
		return chanceVariable;
	}

	@Override
	public boolean hasAnySubIntervention() {
		int i;
		for (i = 0; i < interventions.length  && 
				(chanceVariableDistribution[i] == 0.0 || interventions[i] == null); i++);
		return i < interventions.length;
	}

	@Override
	protected int getNumLeaves() {
		int numberOfLeaves = 0;
		List<Intervention> distinctInterventions = new ArrayList<Intervention>();
		for (int i = 0; i < interventions.length; i++) {
			if (interventions[i] != null && chanceVariableDistribution[i] != 0.0) {
				boolean alreadyIncluded = false;
				int interventionIndex = 0;
				int numDistinctInterventions = distinctInterventions.size();
				while (interventionIndex < numDistinctInterventions && !alreadyIncluded) {
					alreadyIncluded |= distinctInterventions.get(interventionIndex++).sameAs(interventions[i]);
				}
				if (!alreadyIncluded) {
					distinctInterventions.add(interventions[i]);
					numberOfLeaves += interventions[i].getNumLeaves();
				}
			}
		}
		if (numberOfLeaves == 0) {// If no sub-leaves, 
			numberOfLeaves = 1;   // this is a leaf.
		}
		return numberOfLeaves;
	}
	


}
