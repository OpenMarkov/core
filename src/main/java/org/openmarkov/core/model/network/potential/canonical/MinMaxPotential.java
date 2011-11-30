package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FSPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;


public abstract class MinMaxPotential extends ICIPotential {

	// Constants
	/** Constant defined to manipulate sub-potential variables */
	protected final int CONDITIONED_VAR_POSITION = 0;
	
	/** Constant defined to manipulate sub-potential variables */
	protected final int CONDITIONING_VAR_POSITION = 1;
	
	// Attributes
	/** The pseudoVariable is used in the factorization
	 * of the noisy MAX/MIN proposed by D&iacute;ez and 
	 * Gal&aacuate;n (2003).
	 * @frozen */
	protected Variable pseudoVariable;
	
	// Constructor
	public MinMaxPotential(ICIModelType model, ArrayList<Variable> variables, 
			PotentialRole role) {
		// In principle, role will be "conditional probability"
		super(model, variables, role);
		Variable conditionedVariable = variables.get(0);
		String psedoVariableName = "pseudo-" + conditionedVariable.getName();
		// TODO Comprobar que no existe otra variable que tenga el mismo nombre
		pseudoVariable = new Variable(psedoVariableName,
				conditionedVariable.getNumStates());
	}

	// Methods
	/** @return Delta<sub>Y</sub> potential. <code>TablePotential</code> */
	protected abstract TablePotential getDeltaPotential() 
			throws NotEnoughMemoryException;
	
	/** @return C<sub>y</sub><sup>x<sub>i</sub></sup> potential. 
	 *  <code>TablePotential</code> */
	protected abstract TablePotential accruedPotential(
			TablePotential potential) throws NotEnoughMemoryException;

	public ArrayList<TablePotential> getAccruedPotentials()
			throws NotEnoughMemoryException {
		ArrayList<TablePotential> accruedPotentials = 
			new ArrayList<TablePotential>(subPotentials.size());
		for (TablePotential subPotential : subPotentials) {
			accruedPotentials.add(accruedPotential(subPotential));
		}
		return accruedPotentials;
	}
	
	/** @return Given a model in witch A->D and B->D, this method returns:
	 *  delta<sub>D,D'</sub>, C<sub>D'</sub><sup>A</sup>, 
	 *  C<sub>D'</sub><sup>B</sup>, C<sub>D</sub><sup>*</sup>.
	 *  <code>ArrayList</code> of <code>TablePotential</code> 
	 * @throws NotEnoughMemoryException */
	public ArrayList<TablePotential> getTablePotentials() 
			throws NotEnoughMemoryException {
		ArrayList<TablePotential> iCIPotentials = 
			new ArrayList<TablePotential>();
		iCIPotentials.add(getDeltaPotential());
		// subPotentials must be of sub-type TablePotential
		for (FSPotential potential : subPotentials) {
			iCIPotentials.add(accruedPotential(
				(TablePotential)potential));
		}
		return iCIPotentials;
	}
	
	/** @return The accrued potentials plus the Delta potential, 
	 * all of them projected onto the evidence
	 * @throws WrongCriterionException 	
	 * 
	 */
	public ArrayList<TablePotential> tableProject(EvidenceCase evidence, 
			InferenceOptions inferenceOptions) 
			throws NotEnoughMemoryException, WrongCriterionException {
		ArrayList<TablePotential> potentials = 
			new ArrayList<TablePotential>(subPotentials.size() + 1);
		potentials.add(getDeltaPotential().tableProject(evidence, null).get(0));
		for (TablePotential subPotential : subPotentials) {
			TablePotential accruedPotential = 
				accruedPotential(subPotential);
			potentials.add(accruedPotential.tableProject(evidence, null).get(0));
		}
		return potentials;
	}
	
	/**	@return The conditional probability table given by this potential
	 * 
	 */
	public TablePotential getCPT() throws NotEnoughMemoryException {
		ArrayList<Variable> variablesToEliminate = new ArrayList<Variable>(1);
		variablesToEliminate.add(pseudoVariable);
		
		ArrayList<TablePotential> potentials = getAccruedPotentials();
		potentials.add(getDeltaPotential());

		return (TablePotential)DiscretePotentialOperations
			.multiplyAndMarginalize(
				potentials, variables, variablesToEliminate);
	}

	public Variable getPseudoVariable() {
		return pseudoVariable;
	}
	
}
