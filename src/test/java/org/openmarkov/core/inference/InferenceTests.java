/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.inference;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.potential.canonical.CanonicalTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CanonicalTests.class,
        })
        
/** @author manuel */
public class InferenceTests {
 
    /** Runs the test suite using the textual runner. */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	public static Test suite() {
		return new JUnit4TestAdapter(InferenceTests.class);
	}

}
