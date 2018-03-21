/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.util.List;

public class ProbNetInfo {
	private ProbNet probNet;
	private List<EvidenceCase> evidence;

	/**
	 * Constructor for ProbNetInfo.
	 *
	 * @param probNet
	 * @param evidence
	 */
	public ProbNetInfo(ProbNet probNet, List<EvidenceCase> evidence) {
		this.probNet = probNet;
		this.evidence = evidence;
	}

	public int hashCode() {
		int hashProbNet = probNet != null ? probNet.hashCode() : 0;
		int hashEvidence = evidence != null ? evidence.hashCode() : 0;

		return (hashProbNet + hashEvidence) * hashEvidence + hashProbNet;
	}

	public boolean equals(Object other) {
		if (other instanceof ProbNetInfo) {
			ProbNetInfo otherProbNetInfo = (ProbNetInfo) other;
			return (
					(
							this.probNet == otherProbNetInfo.probNet || (
									this.probNet != null && otherProbNetInfo.probNet != null && this.probNet
											.equals(otherProbNetInfo.probNet)
							)
					) && (
							this.evidence == otherProbNetInfo.evidence || (
									this.evidence != null && otherProbNetInfo.evidence != null && this.evidence
											.equals(otherProbNetInfo.evidence)
							)
					)
			);
		}

		return false;
	}

	public String toString() {
		return "(" + probNet + ", " + evidence + ")";
	}

	/**
	 * Returns the probNet.
	 *
	 * @return the probNet.
	 */
	public ProbNet getProbNet() {
		return probNet;
	}

	/**
	 * Returns the evidence.
	 *
	 * @return the evidence.
	 */
	public List<EvidenceCase> getEvidence() {
		return evidence;
	}

}