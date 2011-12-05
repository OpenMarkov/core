package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.OpenMarkovTests;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/** @author manuel
 * @author fjdiez */
public class TablePotentialTest {
    
	private TablePotential tablePotential1;

	private TablePotential tablePotential2;

	private TablePotential tablePotential3;

	private TablePotential tablePotential4;

	/** Two binary variables: fsVariable1 and fsVariable2. */
	private TablePotential tablePotential5;

	private ArrayList<Variable> fsVariables1;
	
	private ArrayList<Variable> fsVariables2;
	
	private ArrayList<Variable> fsVariables3;
	
	private ArrayList<Variable> fsVariables4;
	
	private ArrayList<Variable> fsVariables5;

	private Variable fsVariable1;

	private Variable fsVariable2;

	private Variable fsVariable3;

	private Variable fsVariable4;

	private Variable fsVariableB;

	private Variable fsVariableC;

	private Variable fsVariableD;

	private Variable fsVariableA;

	private State[] states1;

	private State[] states2;

	private State[] states3;

	private State[] states4;

	private State[] twoStates;

	private State[] threeStates;

	private State[] fourStates;

	private State[] fiveStates;

	private Finding finding1;

	/** Two binary variables: fsVariable2 = 1, fsVariable4 = 0. */
	private EvidenceCase evidenceCase;
	
	@Before
    public void setUp() throws Exception {
        states1 = new State[]{new State("S1V1"), new State("S2V1")};
        states2 = new State[]{new State("S1V2"), new State("S2V2")};
        states3 = new State[]{new State("S1V3"), new State("S2V3")};
        states4 = new State[]{new State("S1V4"), new State("S2V4")};
        twoStates = new State[]{new State("S1Vx"), new State("S2Vx")};
        threeStates = new State[]{new State("S1Vx"), new State("S2Vx"),
        		new State( "S3Vx")};
        fourStates = new State[]{new State("S1Vx"),new State("S2Vx"), 
        		new State("S3Vx"),new State( "S4Vx")};
        fiveStates = new State[]{new State("S1Vx"),new State( "S2Vx"),
        		new State("S3Vx"), new State("S4Vx"), new State("S5Vx")};
        fsVariable1 = new Variable("V1", states1);
        fsVariable2 = new Variable("V2", states2);
        fsVariable3 = new Variable("V3", states3);
        fsVariable4 = new Variable("V4", states4);
        fsVariableB = new Variable("VB", twoStates);
        fsVariableD = new Variable("VD", threeStates);
        fsVariableA = new Variable("VA", fourStates);
        fsVariableC = new Variable("VC", fiveStates);
        fsVariables1 = new ArrayList<Variable>();
        fsVariables2 = new ArrayList<Variable>();
        fsVariables3 = new ArrayList<Variable>();
        fsVariables4 = new ArrayList<Variable>();
        fsVariables5 = new ArrayList<Variable>();
        fsVariables1.add(fsVariable2); // B (2 states)
        fsVariables1.add(fsVariable4); // D (2 states)
        fsVariables1.add(fsVariable1); // A (2 states)
        fsVariables1.add(fsVariable3); // C (2 states)
        fsVariables2.add(fsVariable1); // A (2 states)
        fsVariables2.add(fsVariable2); // B (2 states)
        fsVariables2.add(fsVariable3); // C (2 states)
        fsVariables3.add(fsVariableB); // B (2 states) 
        fsVariables3.add(fsVariableD); // D (3 states) 
        fsVariables3.add(fsVariableA); // A (4 states) 
        fsVariables3.add(fsVariableC); // C (5 states) 
        fsVariables4.add(fsVariableA); // A (4 states) 
        fsVariables4.add(fsVariableB); // B (s states) 
        fsVariables4.add(fsVariableC); // C (5 states)
        fsVariables5.add(fsVariable1); // 1 (2 states)
        fsVariables5.add(fsVariable2); // 2 (2 states)
        try {
            tablePotential1 = new TablePotential(
            		fsVariables1, PotentialRole.CONDITIONAL_PROBABILITY);
            tablePotential2 = new TablePotential(
            		fsVariables2, PotentialRole.CONDITIONAL_PROBABILITY);
            tablePotential3 = new TablePotential(
            		fsVariables3, PotentialRole.CONDITIONAL_PROBABILITY);
            tablePotential4 = new TablePotential(
            		fsVariables4, PotentialRole.CONDITIONAL_PROBABILITY);
            tablePotential5 = new TablePotential(
            		fsVariables5, PotentialRole.CONDITIONAL_PROBABILITY);
            // initialize tables
            double[] table = tablePotential4.values;
            for (int i = 0; i < table.length; i++) {
            	table[i] = new Double(i);
            }
            table = tablePotential5.values;
            for (int i = 0; i < table.length; i++) {
            	table[i] = new Double(i);
            }
        } catch (Exception e) {
        	System.err.println(e.getMessage());
			System.err.println(e.getStackTrace());
        }
        finding1 = new Finding(fsVariable2, 1);
        evidenceCase = new EvidenceCase();
        Finding finding1 = new Finding(fsVariable2, 1);
        Finding finding2 = new Finding(fsVariable4, 0);
        evidenceCase.addFinding(finding1);
        evidenceCase.addFinding(finding2);
    }

    @Test
    public void testTablePotential() {
        assertEquals(16 ,tablePotential1.values.length);
    }

	/** test the offsets array length and the value of each offset */
    @Test
    public void testGetOffsets() {
		int[] offsets = tablePotential1.getOffsets();
		assertEquals(4, offsets.length);
		assertEquals(1, offsets[0]);
		assertEquals(2, offsets[1]);
		assertEquals(4, offsets[2]);
		assertEquals(8, offsets[3]);
    }

    @Test
    public void testGetTable() {
    	double[] table = tablePotential1.getValues();
    	assertEquals(16, table.length);
    }
    
    @Test
    public void testGetAccumulateOffsets() throws NotEnoughMemoryException, NoFindingException, WrongCriterionException {
    	// tablePotential1 contains B,D,A,C. Dimensions (2,2,2,2)
    	// tablePotential2 contains A,B,C. Dimensions (2,2,2)
    	int[] accOffsets = tablePotential1.getAccumulatedOffsets(
    		tablePotential2.getVariables());
    	
    	assertEquals(4, accOffsets.length);
    	
    	assertEquals(2, accOffsets[0]);
    	assertEquals(-2, accOffsets[1]);
    	assertEquals(-1, accOffsets[2]);
    	assertEquals(1, accOffsets[3]);
    	
    	// tablePotential3 contains B,D,A,C. Dimensions (2,3,4,5)
    	// tablePotential4 contains A,B,C. Dimensions (4,2,5)
    	
    	accOffsets = tablePotential3.getAccumulatedOffsets(
        		tablePotential4.getVariables());
        	
       	assertEquals(4, accOffsets.length);
        	
       	assertEquals(4, accOffsets[0]);
       	assertEquals(-4, accOffsets[1]);
       	assertEquals(-3, accOffsets[2]);
       	assertEquals(1, accOffsets[3]);
       	
       	// test getAccumulatedOffsets with previous projected tablePotentials
        evidenceCase = new EvidenceCase();
        finding1 = new Finding(fsVariableD, 1);
        try {
			evidenceCase.addFinding(finding1);
		} catch (InvalidStateException e) {
			// Unreachable code
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			// Unreachable code
			e.printStackTrace();
		}
       	ArrayList<TablePotential> projected = 
       		tablePotential3.tableProject(evidenceCase, null);
       	TablePotential tp3Projected = projected.get(0);
       	projected = tablePotential4.tableProject(evidenceCase, null);
       	TablePotential tp4Projected = (TablePotential)projected.get(0);
       	
       	accOffsets = tp3Projected.getAccumulatedOffsets(
        		tp4Projected.getVariables());

       	assertEquals(3, accOffsets.length);
    	
    	assertEquals(4, accOffsets[0]);
    	assertEquals(-3, accOffsets[1]);
    	assertEquals(1, accOffsets[2]);
    	
        evidenceCase = new EvidenceCase();
    	Finding finding2 = new Finding(fsVariableB, 1);
    	try {
			evidenceCase.addFinding(finding2);
		} catch (InvalidStateException e) {
			// Unreachable code
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			// Unreachable code
			e.printStackTrace();
		}
    	
       	projected = tp3Projected.tableProject(evidenceCase, null);
    	tp3Projected = (TablePotential)projected.get(0);
       	projected = tp4Projected.tableProject(evidenceCase, null);
       	tp4Projected = (TablePotential)projected.get(0);
       	
       	accOffsets = tp3Projected.getAccumulatedOffsets(
        		tp4Projected.getVariables());
       	
       	assertEquals(2, accOffsets.length);
    	
    	assertEquals(1, accOffsets[0]);
    	assertEquals(1, accOffsets[1]);
    }

    /*@Test
    /** tablePotential5 has two variables: fsVariable1 and fsVariable2, each one
     *  with 2 states.<p>
     *  evidenceCase: fsVariable2 = 1, fsVariable4 = 0. */
    public void testProject1() 
    		throws NotEnoughMemoryException, NoFindingException, WrongCriterionException {
    	// Projection
    	ArrayList<TablePotential> projectedPotentials = 
    		tablePotential5.tableProject(evidenceCase, null); // fsVariable2 = 1;
    	
    	// Test number of projected potentials
    	assertEquals(1, projectedPotentials.size());
    	TablePotential projected = (TablePotential)projectedPotentials.get(0);

    	// Test projected variables
    	assertEquals(1, projected.getVariables().size());
    	assertEquals(fsVariable1, projected.getVariables().get(0));

    	// Test same table as original potential
    	double[] tableProjected = projected.values;
    	assertEquals(tablePotential5.values, tableProjected);

    	// Initial position
    	int initialPosition = projected.getInitialPosition();
    	assertEquals(2, initialPosition);
    	
    	// Offsets
    	int[] offsets = projected.getOffsets();
    	
    	// Test table content
    	assertEquals(1, offsets.length);
    	assertEquals(1, offsets[0]);
    	assertEquals(2.0, tableProjected[initialPosition]);
    	assertEquals(3.0, tableProjected[initialPosition + offsets[0]]);
    }
   
    @Test
    /** tablePotential5 has two variables: fsVariable1 and fsVariable2, each one
     *  with 2 states.<p>
     *  evidenceCase: fsVariable2 = 1, fsVariable4 = 0. */
    public void testProject2() 
    		throws NotEnoughMemoryException, NoFindingException, WrongCriterionException {
    	// Projection
    	ArrayList<TablePotential> projectedPotentials = 
    		tablePotential5.tableProject(evidenceCase, null); // fsVariable2 = 1;
    	
    	// Test number of projected potentials
    	assertEquals(1, projectedPotentials.size());
    	TablePotential projected = (TablePotential)projectedPotentials.get(0);

    	// Test projected variables
    	assertEquals(1, projected.getVariables().size());
    	assertEquals(fsVariable1, projected.getVariables().get(0));

    	// Test projected potential size
    	double[] tableProjected = projected.values;
    	assertEquals(tableProjected.length, 2);

    	// Initial position
    	int initialPosition = projected.getInitialPosition();
    	assertEquals(0, initialPosition);
    	
    	// Offsets
    	int[] offsets = projected.getOffsets();
    	
    	// Test table content
    	assertEquals(1, offsets.length,OpenMarkovTests.maxError);
    	assertEquals(1, offsets[0],OpenMarkovTests.maxError);
    	assertEquals(2.0, tableProjected[initialPosition],OpenMarkovTests.maxError);
    	assertEquals(3.0, tableProjected[initialPosition + offsets[0]],OpenMarkovTests.maxError);
    }
    
    @Test
    /** Test multiplication of projected potentials. */
    public void testMultiplicationProjected() 
    		throws NotEnoughMemoryException, NoFindingException, WrongCriterionException {
    	int dimA = 3;
    	int dimB = 2;
    	int dimC = 3;
    	Variable A = new Variable("A", dimA);
    	Variable B = new Variable("B", dimB);
    	Variable C = new Variable("C", dimC);
    	ArrayList<Variable> variablesTPAB = new ArrayList<Variable>();
    	variablesTPAB.add(A);
    	variablesTPAB.add(B);
    	ArrayList<Variable> variablesTPCBA = new ArrayList<Variable>();
    	variablesTPCBA.add(C);
    	variablesTPCBA.add(B);
    	variablesTPCBA.add(A);
    	double[] tableAB = new double[]{0.2, 0.1, 0.7, 0.6, 0.4, 0.0};
    	TablePotential tpAB = new TablePotential(
    			variablesTPAB, PotentialRole.CONDITIONAL_PROBABILITY, tableAB);
    	double[] tableCBA = new double[]{0.0, 0.1, 0.9, 0.7, 0.1, 0.2, 0.6, 0.2,
    			0.2, 0.15, 0.35, 0.5, 0.55, 0.25, 0.2, 0.85, 0.05, 0.1};
    	TablePotential tpCBA = new TablePotential(
    			variablesTPCBA, PotentialRole.CONDITIONAL_PROBABILITY, tableCBA);
    	
    	Finding findingA1 = new Finding(A, 1);
    	HashMap<Variable, Finding> findingsA1 =new HashMap<Variable, Finding>();
    	findingsA1.put(A, findingA1);
    	EvidenceCase evidenceCaseA1 = new EvidenceCase(findingsA1);
    	
    	ArrayList<TablePotential> projectedPotentials = 
    		tpAB.tableProject(evidenceCaseA1, null);
    	TablePotential projectedPotentialB = 
    		(TablePotential)projectedPotentials.get(0);
    	projectedPotentials = tpCBA.tableProject(evidenceCaseA1, null);
    	
    	// Test multiply projected potentials
    	projectedPotentials.add(projectedPotentialB);
    	TablePotential multiplication = null;
    	try {
			multiplication = 
				DiscretePotentialOperations.multiply(projectedPotentials);
		} catch (NotEnoughMemoryException e) {
			fail("Not enough memory for test");
		}
		
		// Test variables
		ArrayList<Variable> variables = multiplication.getVariables();
		assertEquals(2, variables.size());
		assertTrue(variables.contains(B));
		assertTrue(variables.contains(C));
		
		// Test table
		assertEquals(multiplication.values.length, 6);
		assertEquals(multiplication.values[0], 0.01,OpenMarkovTests.maxError);
		assertEquals(multiplication.values[5], 0.2,OpenMarkovTests.maxError);
    }
    
    @Test
    public void testGetInitialPosition() {
    	assertEquals(0, tablePotential1.getInitialPosition());
    }

/*    @Test
    /** Test accumulated offsets in projected potentials. */
/*    public void testGetAccumulateOffsetsProjected() 
    		throws NotEnoughMemoryException, NoFindingException {
		// Create data
		// Variables
		Variable A = new Variable("A", 2);
		Variable B = new Variable("B", 2);
		Variable C = new Variable("C", 2);
		// Arrays of variables
		ArrayList<Variable> baPotentialVariables = new ArrayList<Variable>();
		baPotentialVariables.add(B);
		baPotentialVariables.add(A);
		ArrayList<Variable> cabPotentialVariables = new ArrayList<Variable>();
		cabPotentialVariables.add(C);
		cabPotentialVariables.add(A);
		cabPotentialVariables.add(B);
		ArrayList<Variable> bcPotentialVariables = new ArrayList<Variable>();
		bcPotentialVariables.add(B);
		bcPotentialVariables.add(C);
		// table of potentials
		double[] baTable = {0.7, 0.3, 0.9, 0.1};
		double[] cabTable = {0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
		// tablePotentials
		TablePotential cabPotential = 
			new TablePotential(cabPotentialVariables, cabTable);
		TablePotential bcPotential = 
			new TablePotential(bcPotentialVariables, baTable);
		// evidence case
		Finding a0Finding = new Finding(A, 0);
		Finding b1Finding = new Finding(B, 1);
		EvidenceCase evidenceCaseA0 = new EvidenceCase();
		evidenceCaseA0.addFinding(a0Finding);
		EvidenceCase evidenceCaseB1 = new EvidenceCase();
		evidenceCaseB1.addFinding(b1Finding);
		// project potentials
		TablePotential cbPotential = 
			(TablePotential)cabPotential.project(evidenceCaseA0).get(0);
    	
		ArrayList<Variable> otherVariables = new ArrayList<Variable>();
		otherVariables.add(B);
		otherVariables.add(C);
		int[] cabAccOffsetsNotProjected = bcPotential
			.getAccumulatedOffsets(cabPotential.getVariables());
		int[] cbAccOffsetsProjected = bcPotential			
			.getAccumulatedOffsets(cbPotential.getOriginalVariables());
		int cabAccOffsetsLength = cabAccOffsetsNotProjected.length;
		assertEquals(cabAccOffsetsLength, cbAccOffsetsProjected.length);
		for(int i = 0; i < cabAccOffsetsLength; i++) {
			assertEquals(cabAccOffsetsNotProjected[i], cbAccOffsetsProjected[i]);
		}
    }*/
    
}
