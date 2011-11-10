package org.openmarkov.core.model.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.xml.sax.XMLReader;

public class EvidenceCaseTest {

	@Test public void extendEvidence() throws NotEnoughMemoryException, 
	IOException, ParserException, ProbNodeNotFoundException, 
	ConstraintViolationException, IncompatibleEvidenceException,
	InvalidStateException, WrongCriterionException, NullPointerException, JDOMException {
		ProbNet probNet = XMLReader.getUniqueInstance().
			loadProbNet(IOTests.testsPath + "mini-net.xml");
		assertNotNull(probNet);
		EvidenceCase evidence = new EvidenceCase();
		Variable A = probNet.getVariable("A");
		assertNotNull(A);
		Finding aFinding = new Finding(A, 0);
		evidence.addFinding(aFinding);
		evidence.extendEvidence(probNet);
		assertEquals(2, evidence.getFindings().size());
		Variable B = probNet.getVariable("B");
		Finding bFinding = evidence.getFinding(B);
		assertNotNull(bFinding);
		assertEquals(1, bFinding.getStateIndex());
	}
	
}
