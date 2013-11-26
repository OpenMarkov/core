package org.openmarkov.core.dt;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

public class DecisionTreeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDecideTestID() {
		ProbNet decideTestID = NetsFactory
				.createInfluenceDiagramDecisionTestProblem(0.14, 0.91, 0.97);
		
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestID);
		Assert.assertEquals(94.312, decisionTree.getUtility(), 0.0001);
	}

	@Test
	public void testDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = buildDecideTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestDAN);
		Assert.assertEquals(94.312, decisionTree.getUtility(), 0.0001);
	}
	
	@Test
	public void testDatingTestDAN() throws NodeNotFoundException {
		ProbNet datingDAN = buildDatingDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(datingDAN);
		Assert.assertEquals(9.4016, decisionTree.getUtility(), 0.0001);
	}	

	private ProbNet buildDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableY = new Variable("Y", "negative", "positive");
		Variable variableD = new Variable("D","no","yes");
		Variable variableT = new Variable("T","no","yes");
		Variable variableU1 = new Variable("U1");
		Variable variableU2 = new Variable("U2");
		
		ProbNode nodeX = decideTestDAN.addProbNode(variableX, NodeType.CHANCE);
		ProbNode nodeY = decideTestDAN.addProbNode(variableY, NodeType.CHANCE);
		ProbNode nodeU1 = decideTestDAN.addProbNode(variableU1, NodeType.UTILITY);
		ProbNode nodeU2 = decideTestDAN.addProbNode(variableU2, NodeType.UTILITY);
		ProbNode nodeD = decideTestDAN.addProbNode(variableD, NodeType.DECISION);
		decideTestDAN.addProbNode(variableT, NodeType.DECISION);
		
		decideTestDAN.getGraph().makeLinksExplicit(false);
		decideTestDAN.addLink(variableX, variableY, true);
		decideTestDAN.addLink(variableX, variableU1, true);
		decideTestDAN.addLink(variableD, variableY, true);
		decideTestDAN.addLink(variableD, variableT, true);
		decideTestDAN.addLink(variableD, variableU2, true);
		decideTestDAN.addLink(variableT, variableU1, true);
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialY = new TablePotential(Arrays.asList(variableY, variableD, variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialY.values = new double [] {0, 0, 0.97, 0.03, 0, 0, 0.09, 0.91};
		nodeY.setPotential(potentialY);
		
		TablePotential potentialU1 = new TablePotential(variableU1, Arrays.asList(variableX, variableT));
		potentialU1.values = new double [] {100, 30, 90, 80};
		nodeU1.setPotential(potentialU1);		
		
		TablePotential potentialU2 = new TablePotential(variableU1, Arrays.asList(variableD));
		potentialU2.values = new double [] {0, -2};
		nodeU2.setPotential(potentialU2);		
		
		Link link = decideTestDAN.getGraph().getLink(nodeD.getNode(), nodeY.getNode(), true);
		link.initializesRestrictionsPotential();
		TablePotential restrictionsPotential = (TablePotential)link.getRestrictionsPotential();
		restrictionsPotential.values = new double[]{0,1,0,1};
		
		link.setRevealingStates(Arrays.asList(variableD.getStates()[1]));
		
		return decideTestDAN;
	}
	
	private ProbNet buildDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable variableAsk = new Variable("Ask", "no", "yes");
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableAccept = new Variable("Accept", "no", "yes");
		Variable variableLikesMe = new Variable("LikesMe","no","yes");
		Variable variableToDo = new Variable("ToDo","restaurant","movie");
		Variable variableTV = new Variable("TV","good","bad");
		Variable variableTVExp = new Variable("TVExp","negative","positive");
		Variable variableClub = new Variable("Club","negative","positive");
		Variable variableMeetFr = new Variable("MeetFr","negative","positive");
		Variable variableNCExp = new Variable("NCExp","negative","positive");
		Variable variableMovie = new Variable("Movie","romantic","action");
		Variable variableRest = new Variable("Rest","cheap","expensive");
		Variable variableMMood = new Variable("mMood","bad","good");
		Variable variableRMood = new Variable("rMood","bad","good");
		Variable variableMExp = new Variable("mExp","negative","positive");
		Variable variableRExp = new Variable("rExp","negative","positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rCExp");
		
		ProbNode nodeAsk = datingDAN.addProbNode(variableAsk, NodeType.DECISION);
		ProbNode nodeNClub = datingDAN.addProbNode(variableNClub, NodeType.DECISION);
		ProbNode nodeAccept = datingDAN.addProbNode(variableAccept, NodeType.CHANCE);
		ProbNode nodeLikesMe = datingDAN.addProbNode(variableLikesMe, NodeType.CHANCE);
		ProbNode nodeToDo = datingDAN.addProbNode(variableToDo, NodeType.CHANCE);
		ProbNode nodeTV = datingDAN.addProbNode(variableTV, NodeType.CHANCE);
		ProbNode nodeTVExp = datingDAN.addProbNode(variableTVExp, NodeType.CHANCE);
		ProbNode nodeClub = datingDAN.addProbNode(variableClub, NodeType.CHANCE);
		ProbNode nodeMeetFr = datingDAN.addProbNode(variableMeetFr, NodeType.CHANCE);
		ProbNode nodeNCExp = datingDAN.addProbNode(variableNCExp, NodeType.CHANCE);
		ProbNode nodeMovie = datingDAN.addProbNode(variableMovie, NodeType.DECISION);
		ProbNode nodeRest = datingDAN.addProbNode(variableRest, NodeType.DECISION);
		ProbNode nodeMMood = datingDAN.addProbNode(variableMMood, NodeType.CHANCE);
		ProbNode nodeRMood = datingDAN.addProbNode(variableRMood, NodeType.CHANCE);
		ProbNode nodeMExp = datingDAN.addProbNode(variableMExp, NodeType.CHANCE);
		ProbNode nodeRExp = datingDAN.addProbNode(variableRExp, NodeType.CHANCE);
		ProbNode nodeUTVExp = datingDAN.addProbNode(variableUTVExp, NodeType.UTILITY);
		ProbNode nodeUNCExp = datingDAN.addProbNode(variableUNCExp, NodeType.UTILITY);
		ProbNode nodeUmExp = datingDAN.addProbNode(variableUmExp, NodeType.UTILITY);
		ProbNode nodeUrExp = datingDAN.addProbNode(variableUrExp, NodeType.UTILITY);
		
		datingDAN.getGraph().makeLinksExplicit(false);
		datingDAN.addLink(variableAsk, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);
		datingDAN.addLink(variableAccept, variableNClub, true);
		datingDAN.addLink(variableAccept, variableToDo, true);
		datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMMood, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMMood, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRMood, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRMood, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);
		
		TablePotential potentialAccept = new TablePotential(Arrays.asList(variableAccept, variableAsk, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialAccept.values = new double [] {1, 0, 0.99, 0.01, 1, 0, 0.9, 0.1};
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);
		
		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.values = new double [] {0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85};
		nodeToDo.setPotential(potentialToDo);		
		
		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.values = new double [] {0.25, 0.75, 0.88, 0.12};
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.values = new double [] {0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99};
		nodeMExp.setPotential(potentialMExp);

		TablePotential potentialUmExp = new TablePotential(variableUmExp, Arrays.asList(variableMExp));
		potentialUmExp.values = new double [] {-10, 10};
		nodeUmExp.setPotential(potentialUmExp);
		
		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.values = new double [] {0.5, 0.5, 0.2, 0.8};
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.values = new double [] {0.95, 0.05, 1, 0, 0.01, 0.99, 0.08, 0.92};
		nodeRExp.setPotential(potentialRExp);

		TablePotential potentialUrExp = new TablePotential(variableUrExp, Arrays.asList(variableRExp));
		potentialUrExp.values = new double [] {-10, 10};
		nodeUrExp.setPotential(potentialUrExp);		
		
		TablePotential potentialTV = new TablePotential(Arrays.asList(variableTV), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTV.values = new double [] {0.15, 0.85};
		nodeTV.setPotential(potentialTV);	
		
		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub, variableTV), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.values = new double [] {0, 1, 0, 0, 1, 0, 0, 0};
		nodeTVExp.setPotential(potentialTVExp);		

		TablePotential potentialClub = new TablePotential(Arrays.asList(variableClub, variableNClub), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialClub.values = new double [] {0, 0, 0.22, 0.78};
		nodeClub.setPotential(potentialClub);

		TablePotential potentialMeetFr = new TablePotential(Arrays.asList(variableMeetFr, variableNClub), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMeetFr.values = new double [] {0, 0, 0.16, 0.84};
		nodeMeetFr.setPotential(potentialMeetFr);
		
		ICIPotential potentialNCExp = new MaxPotential(Arrays.asList(variableNCExp, variableMeetFr, variableClub));
		potentialNCExp.setLeakyParameters(new double [] {0.99, 0.01});
		nodeNCExp.setPotential(potentialNCExp);			
		
		TablePotential potentialUTVExp = new TablePotential(variableUTVExp, Arrays.asList(variableTVExp));
		potentialUTVExp.values = new double [] {-10, 10};
		nodeUTVExp.setPotential(potentialUTVExp);		

		TablePotential potentialUNCExp = new TablePotential(variableUNCExp, Arrays.asList(variableNCExp));
		potentialUNCExp.values = new double [] {-10, 10};
		nodeUNCExp.setPotential(potentialUNCExp);		
		
		
		Link linkAskAccept = datingDAN.getGraph().getLink(nodeAsk.getNode(), nodeAccept.getNode(), true);
		linkAskAccept.initializesRestrictionsPotential();
		TablePotential restrictionsAskAccept = (TablePotential)linkAskAccept.getRestrictionsPotential();
		restrictionsAskAccept.values = new double[]{1,1,0,1};
		linkAskAccept.setRevealingStates(Arrays.asList(variableAsk.getStates()[0], variableAsk.getStates()[1]));

		Link linkAcceptNClub = datingDAN.getGraph().getLink(nodeAccept.getNode(), nodeNClub.getNode(), true);
		linkAcceptNClub.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptNClub = (TablePotential)linkAcceptNClub.getRestrictionsPotential();
		restrictionsAcceptNClub.values = new double[]{1,0,1,0};

		Link linkAcceptToDo = datingDAN.getGraph().getLink(nodeAccept.getNode(), nodeToDo.getNode(), true);
		linkAcceptToDo.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptToDo = (TablePotential)linkAcceptToDo.getRestrictionsPotential();
		restrictionsAcceptToDo.values = new double[]{0,1,0,1};
		linkAcceptToDo.setRevealingStates(Arrays.asList(variableAccept.getStates()[1]));

		Link linkToDoMovie = datingDAN.getGraph().getLink(nodeToDo.getNode(), nodeMovie.getNode(), true);
		linkToDoMovie.initializesRestrictionsPotential();
		TablePotential restrictionsToDoMovie = (TablePotential)linkToDoMovie.getRestrictionsPotential();
		restrictionsToDoMovie.values = new double[]{0,1,0,1};

		Link linkToDoRest = datingDAN.getGraph().getLink(nodeToDo.getNode(), nodeRest.getNode(), true);
		linkToDoRest.initializesRestrictionsPotential();
		TablePotential restrictionsToDoRest = (TablePotential)linkToDoRest.getRestrictionsPotential();
		restrictionsToDoRest.values = new double[]{1,0,1,0};

		Link linkMovieMEXp = datingDAN.getGraph().getLink(nodeMovie.getNode(), nodeMExp.getNode(), true);
		linkMovieMEXp.setRevealingStates(Arrays.asList(variableMovie.getStates()[0], variableMovie.getStates()[1]));

		Link linkRestREXp = datingDAN.getGraph().getLink(nodeRest.getNode(), nodeRExp.getNode(), true);
		linkRestREXp.setRevealingStates(Arrays.asList(variableRest.getStates()[0], variableRest.getStates()[1]));
		
		Link linkNClubTVExp = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeTVExp.getNode(), true);
		linkNClubTVExp.initializesRestrictionsPotential();
		TablePotential restrictionsNClubTVExp = (TablePotential)linkNClubTVExp.getRestrictionsPotential();
		restrictionsNClubTVExp.values = new double[]{1,0,1,0};
		linkNClubTVExp.setRevealingStates(Arrays.asList(variableNClub.getStates()[0]));

		Link linkNClubClub = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeClub.getNode(), true);
		linkNClubClub.initializesRestrictionsPotential();
		TablePotential restrictionsNClubClub = (TablePotential)linkNClubClub.getRestrictionsPotential();
		restrictionsNClubClub.values = new double[]{0,1,0,1};
		linkNClubClub.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		Link linkNClubMeetFr = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeMeetFr.getNode(), true);
		linkNClubMeetFr.initializesRestrictionsPotential();
		TablePotential restrictionsNClubMeetFr = (TablePotential)linkNClubMeetFr.getRestrictionsPotential();
		restrictionsNClubMeetFr.values = new double[]{0,1,0,1};
		linkNClubMeetFr.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));
		
		nodeTV.setAlwaysObserved(true);
		
		return datingDAN;
	}	
	
}
