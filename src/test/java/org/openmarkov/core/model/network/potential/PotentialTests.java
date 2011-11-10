package org.openmarkov.core.model.network.potential;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

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
