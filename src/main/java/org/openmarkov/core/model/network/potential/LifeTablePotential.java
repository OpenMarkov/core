package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

/**
 * Potential that computes TTE from an extended Life Table type potential.
 * Life tables: In actuarial science and demography, a life table (also called a mortality table
 * or actuarial table) is a table which shows, for each age, what the probability is
 * that a person of that age will die before their next birthday ("probability of death"). Source: Wikipedia.
 *
 * This concept may be extended to a table which represents the probability of an event happening
 * within a determined simulation time interval.
 *
 * The table is organised in the following format:
 * interval_1, probability_interval_1 per cycle
 * interval_2, probability_interval_2 per cycle
 * ...
 * interval_i, probability_interval_i per cycle
 * ...
 * interval_n, probability_last_interval per cycle
 *
 * The cycle units are the same of the rest of the model and are those in which the discount is expressed.
 * length_interval_n ends in  infinite otherwise.
 * interval_1, interval_2,...,interval_n, are consecutive in time and [clock1,clcck2) format
 * When every length_interval_i = 1 we have the standard life table.
 *
 * The TTE is computed by considering a series of exponential distributions representing the probability
 * of an event happening at clock= t conditioned it has not happened in a previous interval. Therefore the simulation
 * is done according the following:
 * 1.- For every interval i the exponential rate lambda_i is computed from probability_interval_i
 * 2.- tte =0; i=0
 * 	   while ( (i< length_interval.size) &&
 * 	   			(( s[i] = sample(Exp(lambda[i])> length_interval[i]) )
 * 	   		         tte += length_interval[i++]
 * 	   tte += s_i
 *
 * FIXME The table can be introduced in the GUI graph or read from a file ??
 * @author cmyago
 * @version 1.0 23/10/2023 -
 *
 */
@PotentialType(name = "Life Table") public class LifeTablePotential extends Potential implements DESSimulablePotential {
	/**
	 * Life table where each row has number is < numberUnits, probabilityByUnit > 
	 */
	 private /*final*/ double[][] lifeTable={{10.0, 0.01}, {Double.MAX_VALUE, 0.001}};

	/**
	 * Creates a new LifeTablePotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of LifeTablePotential
	 * @param role      <code>PotentialRole</code> of LifeTablePotential
	 */
	public LifeTablePotential(List<Variable> variables, PotentialRole role, double[][] lifeTable) {
		super(variables, role);
		this.lifeTable = lifeTable;
	}
	
	/**
	 * Creates a new LifeTablePotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of LifeTablePotential
	 * @param role      <code>PotentialRole</code> of LifeTablePotential
	 */
	public LifeTablePotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);

	}


	/**
	 * Creates a new LifeTablePotential with variables as its list of Variable and role as PotentialRole
	 * @param role      <code>PotentialRole</code> of LifeTablePotential
	 * @param variables  Group of <code>Variable</code> which are the variables of the new LifeTablePotential
	 */
	public LifeTablePotential(PotentialRole role, Variable... variables) {
		this(toList(variables), role);
	}

	/**
	 * Creates a new LifeTablePotential equal to potential
	 * Copy constructor for IncrementPotential
	 *
	 * @param potential LifeTablePotential from which the new LifeTablePotential is created
	 */
	public LifeTablePotential(LifeTablePotential potential) {
		super(potential);
	}

	// Methods

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>
	 * @param role      <code>PotentialRole</code>
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		return (node.getProbNet().getNetworkType() instanceof DESNetworkType) && (node.getNodeType() == NodeType.EVENT);
	}

	@Override
	public double sampleConditionedVariable(double randomNumber, EvidenceCase parents) {


		return 0;
	}

//    @Override
//	public void resetSimulation(){
//
//		incrementedValue =0;
//	}
	// Methods
	@Override
	public List<TablePotential> tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException {
		//TODO
		return null;
	}


	@Override public Potential copy() {
		return new LifeTablePotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	@Override public String toString() {
		return super.toString() + " = Increment";
	}

	@Override public void scalePotential(double scale) {

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		LifeTablePotential potential = (LifeTablePotential) super.deepCopy(copyNet);
		return potential;

	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}
}
