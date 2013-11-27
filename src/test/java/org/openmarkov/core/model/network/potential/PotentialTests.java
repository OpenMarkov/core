/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.potential.treeADD.TreeADDPotentialTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    PotentialTest.class,
    TablePotentialTest.class,
    TreeADDPotentialTest.class
        })
        
/** @author manuel
 * @author fjDiez */
public class PotentialTests {

    /** Runs the test suite using the textual runner. */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	public static Test suite() {
		return new JUnit4TestAdapter(PotentialTests.class);
	}

}
