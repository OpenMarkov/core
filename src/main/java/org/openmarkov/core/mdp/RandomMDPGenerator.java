package org.openmarkov.core.mdp;

import java.util.ArrayList;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;


public class RandomMDPGenerator {

	public static ProbNet getRandomMDP (int numberOfActions, int numberOfStates, int numberOfStateTransitions,
			double minUtility, double maxUtility, int numberOfUtilities) throws Exception {
		MatrizAleatoria []matArray= new MatrizAleatoria[numberOfActions];

		for (int i=0; i< matArray.length; i++) {
			matArray[i]= new MatrizAleatoria(numberOfStates, numberOfStateTransitions);
		}

		ProbNet probNet= new ProbNet();
		probNet.addVariable (new Variable ("X_0", numberOfStates), NodeType.CHANCE);
		probNet.addVariable (new Variable ("X_1", numberOfStates), NodeType.CHANCE);
		probNet.addVariable (new Variable ("D", numberOfActions), NodeType.DECISION);
		probNet.addVariable (new Variable ("U"), NodeType.UTILITY);

		Variable varX0= probNet.getVariable("X_0");
		Variable varX1= probNet.getVariable("X_1");
		Variable varD= probNet.getVariable("D");
		Variable varU= probNet.getVariable("U");

		probNet.addLink(varX0, varX1, true);
		probNet.addLink(varD, varX1, true);
		probNet.addLink(varX1, varU, true);

		ArrayList<Variable> variables= new ArrayList<Variable>();
		variables.add (varX1);
		variables.add (varX0);
		variables.add (varD);

		// Orden de las variables: X1, X0, D
		TablePotential potX1 = new TablePotential(
				variables, PotentialRole.JOIN_PROBABILITY);
		int []coords= new int [3];
		double []table= potX1.getValues();

		for (int d=0; d<numberOfActions; d++) {
			coords[2]= d;

			for (int x0=0; x0<numberOfStates; x0++) {
				coords[1]= x0;

				for (int x1=0; x1<numberOfStates; x1++) {
					coords[0]= x1;
					table [potX1.getPosition(coords)]= matArray[d].matriz[x0][x1];
				}				
			}
		}

		probNet.addPotential(potX1);

		variables.clear();
		variables.add(varX1);
		double []utilityTable= new double [numberOfStates];

		for (int i=0; i<numberOfUtilities; i++) {
			int k= (int) (numberOfStates*Math.random());
			utilityTable[k]= (maxUtility-minUtility)*Math.random() - minUtility; 
		}


		TablePotential potU = new TablePotential(
				variables, PotentialRole.UTILITY, utilityTable);
		potU.setUtilityVariable(varU);

		probNet.addPotential(potU);

		return probNet;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int numBackups_mpi= 0;
		int numBackups_mpi_j= 0;
		int mejora= 0;

		for (int i=0; i<100; i++) {
			ProbNet mdp= RandomMDPGenerator.getRandomMDP(4, 6, 2, -5.0, +75.0, 2);
			//System.out.println(mdp);

			ModifiedPolicyIteration mpi= new ModifiedPolicyIteration (mdp);
			mpi.run (new MDPParamsMPI());

			System.err.println (mpi.getStats().getNumIterations()+" iteraciones");
			System.err.println (mpi.getStats().getNumBackups()+" backups");
			System.err.println (mpi.getValueFunction().toString());
			System.err.println ("Politica\n" + mpi.getPolicy().toString());	
			numBackups_mpi+= mpi.getStats().getNumBackups();
			
			ModifiedPolicyIterationWithJumps mpi_j= new ModifiedPolicyIterationWithJumps (mdp);
			mpi_j.run (new MDPParamsMPI());

			System.err.println (mpi_j.getStats().getNumIterations()+" iteraciones");
			System.err.println (mpi_j.getStats().getNumBackups()+" backups");
			System.err.println (mpi_j.getValueFunction().toString());
			System.err.println ("Politica\n" + mpi_j.getPolicy().toString());	
			numBackups_mpi_j+= mpi_j.getStats().getNumBackups();
			
			if (mpi_j.getStats().getNumBackups() < mpi.getStats().getNumBackups()) {
				mejora++;
			}
		}
		
		System.err.println ("\n\nExitos\t: "+ mejora);
		System.err.println ("\nBK MPI\t: " + numBackups_mpi/100);	
		System.err.println ("\nBK MPI_j\t: " + numBackups_mpi_j/100);	
	}

}
