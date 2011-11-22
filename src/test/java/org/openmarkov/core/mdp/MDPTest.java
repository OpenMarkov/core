package org.openmarkov.core.mdp;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmarkov.core.model.network.ProbNet;

public class MDPTest  {
	
	@Test
	public void testValueIteration01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		ValueIteration vi= new ValueIteration(mdp);
		vi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( vi.getMdpVariablePriori(), vi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(vi.getPolicy()));
	}

	@Test
	public void testValueIteration02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		ValueIteration vi= new ValueIteration(mdp);
		vi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( vi.getMdpVariablePriori(), vi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(vi.getPolicy()));
	}

	@Test
	public void testValueIterationGS01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		ValueIterationGS viGS= new ValueIterationGS(mdp);
		viGS.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( viGS.getMdpVariablePriori(), viGS.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(viGS.getPolicy()));
	}

	@Test
	public void testValueIterationGS02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		ValueIterationGS viGS= new ValueIterationGS (mdp);
		viGS.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( viGS.getMdpVariablePriori(), viGS.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(viGS.getPolicy()));
	}
	
	@Test
	public void testPolicyIteration01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		PolicyIteration pi= new PolicyIteration(mdp);
		pi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( pi.getMdpVariablePriori(), pi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(pi.getPolicy()));
	}

	@Test
	public void testPolicyIteration02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		PolicyIteration pi= new PolicyIteration(mdp);
		pi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( pi.getMdpVariablePriori(), pi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals(pi.getPolicy()));
	}
	
	@Test
	public void testModifiedPolicyIteration01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setOrderSequence(6, 6);

		ModifiedPolicyIteration mpi= new ModifiedPolicyIteration(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIteration02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setOrderSequence(12, 12);

		ModifiedPolicyIteration mpi= new ModifiedPolicyIteration(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIteration03() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setAdaptativeOrderSequence(0.5);

		ModifiedPolicyIteration mpi= new ModifiedPolicyIteration(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIteration04() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setAdaptativeOrderSequence(0.5);

		ModifiedPolicyIteration mpi= new ModifiedPolicyIteration(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}
	
	@Test
	public void testAsyncValueIteration01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		AsynchronousValueIteration avi= new AsynchronousValueIteration(mdp);
		avi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( avi.getMdpVariablePriori(), avi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (avi.getPolicy()));
	}

	@Test
	public void testAsyncValueIteration02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		AsynchronousValueIteration avi= new AsynchronousValueIteration(mdp);
		avi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (avi.getMdpVariablePriori(), avi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (avi.getPolicy()));
	}
	
	@Test
	public void testAsyncSingleSidePI01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		SingleSidePolicyIteration aspi= new SingleSidePolicyIteration(mdp);
		aspi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( aspi.getMdpVariablePriori(), aspi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (aspi.getPolicy()));
	}

	@Test
	public void testAsyncSingleSidePI02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParams params= new MDPParams();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);

		SingleSidePolicyIteration aspi= new SingleSidePolicyIteration(mdp);
		aspi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (aspi.getMdpVariablePriori(), aspi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (aspi.getPolicy()));
	}
	
	@Test
	public void testModifiedPolicyIterationWithJump01() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setOrderSequence(6, 6);

		ModifiedPolicyIterationWithJumps mpi= new ModifiedPolicyIterationWithJumps(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIterationWithJump02() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setOrderSequence(12, 12);

		ModifiedPolicyIterationWithJumps mpi= new ModifiedPolicyIterationWithJumps(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIterationWithJump03() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_00.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setAdaptativeOrderSequence(0.5);

		ModifiedPolicyIterationWithJumps mpi= new ModifiedPolicyIterationWithJumps(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 1, 0, 0, 0, 0, 1, 2, 0, 3, 1, 0, 0 };
		MDPTablePolicy policy= new MDPTablePolicy ( mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}

	@Test
	public void testModifiedPolicyIterationWithJump04() throws Exception {
		String fullNetName = "src//openmarkov//mdp//nets//DI_temporal_01.xml";

		ProbNet mdp= NetsIO.openNetworkFile(fullNetName);

		MDPParamsMPI params= new MDPParamsMPI();
		params.setEpsilon(0.01);
		params.setDiscountRate(0.99);
		params.setAdaptativeOrderSequence(0.5);

		ModifiedPolicyIterationWithJumps mpi= new ModifiedPolicyIterationWithJumps(mdp);
		mpi.run(params);
		
		int []correctPolicy= { 2, 1, 0 };
		MDPTablePolicy policy= new MDPTablePolicy (mpi.getMdpVariablePriori(), mpi.getMdpActions(), correctPolicy);
		
		assertTrue (policy.equals (mpi.getPolicy()));
	}
}
