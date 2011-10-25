package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FSPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class ICIPotential extends FSPotential {

	protected ICIModel model;
	
	protected ICIFamily family;
	
	/** @frozen */
	protected Variable conditionedVariable;
	
	protected ArrayList<TablePotential> subPotentials;

	// Constructor
	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @param model. <code>ICIModel</code> */
	public ICIPotential(ICIModel model, ArrayList<Variable> variables, 
			PotentialRole role) {
		super(variables, role);
		this.model = model;
		this.family = model.getFamily();
		conditionedVariable = variables.get(0);
		subPotentials = new ArrayList<TablePotential>();
	}

	// Methods
	public abstract TablePotential getCPT() throws NotEnoughMemoryException;

	@Override
	/** @param evidenceCase. <code>EvidenceCase</code>
	 * @return <code>ArrayList</code> of <code>Potential</code> */
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions) 
			throws NotEnoughMemoryException, WrongCriterionException {
		ArrayList<TablePotential> projectedPotentials = 
			new	ArrayList<TablePotential>() ;
		for (TablePotential tp : tableProject(evidenceCase, inferenceOptions)){
			projectedPotentials.add(tp);
		}
		return projectedPotentials;
	}
	
	/** @param potential. <code>Potential</code> */
	public void addSubPotential(TablePotential subPotential) {
		subPotentials.add(subPotential);
	}
	
	/** @return <code>ArrayList</code> of <code>TablePotential</code>. */
	public ArrayList<TablePotential> getSubPotentials() {
		return subPotentials;
	}
	
	/** @param variable. <code>Variable</code>
	 * @return The first subPotential that contains <code>variable</code>.
	 *  <code>TablePotential</code> or <code>null</code> */
	public TablePotential getSubPotential(Variable variable) {
		for (TablePotential potential : subPotentials) {
			if (potential.getVariables().contains(variable)) {
				return potential;
			}
		}
		return null;
	}
	
	/** @return Residual potential. <code>TablePotential</code> */
	public TablePotential getResidualPotential() {
		return subPotentials.get(subPotentials.size() - 1);
	}

	/** @return model. <code>ICIModel</code> */
	public ICIModel getModel() {
		return model;
	}

	/** @return model. <code>ICIModel</code> */
	public ICIFamily getFamily() {
		return model.getFamily();
	}

	// TODO Revisar que devuelve si no haya leaky potential.
	/** @return (Sub)potential that contains the leak probability. 
	 * @throws <code>NotEnoughMemoryException</code> */
	public TablePotential getLeakyPotential() 
			throws NotEnoughMemoryException {
		// Searchs for the potential that depends on only one variable.
		for (Potential potential : subPotentials) {
			if (potential.getVariables().size() == 1) {
				return (TablePotential)potential;
			}
		}
		return null; 
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append("\nFamily: " + family + ". Model: " + model);
		buffer.append("\nNumber of subPotentials: " +  subPotentials.size());
		buffer.append("\nSubpotentials: ");
		for (Potential potential : subPotentials) {
			ArrayList<Variable> potentialVariables = potential.getVariables();
			buffer.append("[");
			int i;
			for (i = 0; i < potentialVariables.size() - 1; i++) {
				buffer.append(potentialVariables.get(i) + ", ");
			}
			buffer.append(potentialVariables.get(i) + "] ");
		}
		buffer.append("\n");
		return buffer.toString();
	}
}
