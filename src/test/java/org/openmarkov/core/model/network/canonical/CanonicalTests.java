package org.openmarkov.core.model.network.canonical;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ICIPotentialTest.class,
	MinMaxPotentialTest.class,
	MaxPotentialTest.class
})

/** @author manuel
 * @author fjDiez */
public class CanonicalTests {

    /** Runs the test suite using the textual runner. */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	public static Test suite() {
		return new JUnit4TestAdapter(CanonicalTests.class);
	}

}
