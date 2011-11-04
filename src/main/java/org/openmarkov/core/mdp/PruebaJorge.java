package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

/** Prueba de clases MDP
 * @author jorgefs
 * @version 0.1
 */
public class PruebaJorge {

	static void testVI (ProbNet mdp) throws Exception {
		ValueIteration vi= new ValueIteration(mdp);
		vi.run (new MDPParams ());
		
		System.err.println (vi.getStats().getNumIterations()+" iteraciones");
		System.err.println (vi.getStats().getNumBackups()+" backups");
		System.err.println (vi.getValueFunction().toString());
		System.err.println ("Politica\n" + vi.getPolicy().toString());			
	}

	static void testVIGaussSeidel (ProbNet mdp) throws Exception {
		ValueIterationGS viGS= new ValueIterationGS(mdp);
		viGS.run (new MDPParams ());
		
		System.err.println (viGS.getStats().getNumIterations()+" iteraciones");
		System.err.println (viGS.getStats().getNumBackups()+" backups");
		System.err.println (viGS.getValueFunction().toString());
		System.err.println ("Politica\n" + viGS.getPolicy().toString());	
	}

	static void testMPI (ProbNet mdp) throws Exception {
		ModifiedPolicyIteration mpi= new ModifiedPolicyIteration (mdp);
		mpi.run (new MDPParamsMPI());
		
		System.err.println (mpi.getStats().getNumIterations()+" iteraciones");
		System.err.println (mpi.getStats().getNumBackups()+" backups");
		System.err.println (mpi.getValueFunction().toString());
		System.err.println ("Politica\n" + mpi.getPolicy().toString());		
	}
	
	static void testAVI (ProbNet mdp) throws Exception {
		AsynchronousValueIteration avi= new AsynchronousValueIteration(mdp);
		avi.run (new MDPParams ());
		
		System.err.println (avi.getStats().getNumIterations()+" iteraciones");
		System.err.println (avi.getStats().getNumBackups()+" backups");
		System.err.println (avi.getValueFunction().toString());
		System.err.println ("Politica\n" + avi.getPolicy().toString());			
	}

	static void testASPI (ProbNet mdp) throws Exception {
		SingleSidePolicyIteration aspi= new SingleSidePolicyIteration(mdp);
		aspi.run (new MDPParams ());
		
		System.err.println (aspi.getStats().getNumIterations()+" iteraciones");
		System.err.println (aspi.getStats().getNumBackups()+" backups");
		System.err.println (aspi.getValueFunction().toString());
		System.err.println ("Politica\n" + aspi.getPolicy().toString());			
	}

	static void testMPI_jump (ProbNet mdp) throws Exception {
		ModifiedPolicyIterationWithJumps mpi= new ModifiedPolicyIterationWithJumps (mdp);
		mpi.run (new MDPParamsMPI());
		
		System.err.println (mpi.getStats().getNumIterations()+" iteraciones");
		System.err.println (mpi.getStats().getNumBackups()+" backups");
		System.err.println (mpi.getValueFunction().toString());
		System.err.println ("Politica\n" + mpi.getPolicy().toString());		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// String fullNetName = "src//openmarkov//pruebas//jorge//nets//DI_temporal_00.xml";
		// String fullNetName = "src//openmarkov//pruebas//jorge//nets//DI_temporal_01.xml";
//		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_02.xml";
		// String fullNetName = "src//openmarkov//pruebas//jorge//nets//1-MDP-no-factorizado.xml";
		// String fullNetName = "src//openmarkov//pruebas//jorge//nets//2-MDP-factorizado.xml";
		
//		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);
//
//		testVI (mdp);
//
//		testVIGaussSeidel(mdp);		
//		testAVI (mdp);
//		testMPI (mdp);
//		testASPI (mdp);
//		
//		testMPI_jump (mdp);
	}
}
