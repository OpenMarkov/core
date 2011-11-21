package org.openmarkov.core.io;

import java.io.IOException;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.model.network.ProbNet;

import org.jdom.JDOMException;

/** Defines methods to read and write a <code>ProbNet</code>. */
public interface InputOutput {

	// Methods
	/** @param netName = path + network name + extension. <code>String</code>
	 * @return The <code>ProbNet</code> readed or <code>null</code> 
	 * @throws JDOMException 
	 * @throws NullPointerException */ 
	public ProbNet loadProbNet(String netName) throws IOException, 
		ParserException, NotEnoughMemoryException, ProbNodeNotFoundException,
		ConstraintViolationException, NullPointerException, JDOMException;
	
	/** @param netName = path + network name + extension.
	 * @param probNet. <code>ProbNet</code> <code>String</code> 
	 * @throws IOException 
	 * @throws WriterException 
	 * @throws NotEnoughMemoryException */ 
	public void writeProbNet(String netName, ProbNet probNet) 
			throws IOException, NotEnoughMemoryException, WriterException;
		
}
