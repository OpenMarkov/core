/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io;

import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.util.List;

public interface ProbNetWriter {

	/**
	 * @param netName = path + network name + extension.
	 * @param probNet <code>ProbNet</code> <code>String</code>
	 */
	void writeProbNet(String netName, ProbNet probNet) throws WriterException;

	/**
	 * @param netName = path + network name + extension.
	 * @param probNet <code>ProbNet</code> <code>String</code>
	 */
	void writeProbNet(String netName, ProbNet probNet, List<EvidenceCase> evidence) throws WriterException;
}
