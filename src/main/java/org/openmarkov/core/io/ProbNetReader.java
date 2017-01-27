/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.io;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;

import java.io.InputStream;

public interface ProbNetReader {

	/** @param netName = path + network name + extension. <code>String</code>
	 * @return A <code>ProbNetInfo</code> or <code>null</code> */
	ProbNetInfo loadProbNetInfo(String netName, InputStream... file) throws ParserException;

	/** @param netName = path + network name + extension. <code>String</code>
	 * @return A <code>ProbNet</code> or <code>null</code> */
	ProbNet loadProbNet(String netName, InputStream... file) throws ParserException;

}