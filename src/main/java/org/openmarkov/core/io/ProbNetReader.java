package org.openmarkov.core.io;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;

public interface ProbNetReader {

	/** @param netName = path + network name + extension. <code>String</code>
	 * @return A <code>ProbNet</code> or <code>null</code> */ 
	public ProbNet loadProbNet(String netName) throws ParserException;
	
}
