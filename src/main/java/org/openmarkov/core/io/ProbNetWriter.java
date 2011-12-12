package org.openmarkov.core.io;

import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.model.network.ProbNet;

public interface ProbNetWriter {

	/** @param netName = path + network name + extension.
	 * @param probNet. <code>ProbNet</code> <code>String</code> */
	public void writeProbNet(String netName, ProbNet probNet) 
			throws WriterException;
		
}
