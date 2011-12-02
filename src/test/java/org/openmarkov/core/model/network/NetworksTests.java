package org.openmarkov.core.model.network;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    //PotentialTests.class,
	ProbNetTest.class,
    VariableTest.class,
    //CanonicalTests.class,
    //ConstraintsTests.class,
    EvidenceCaseTest.class
        })
        
/** @author manuel
 * @author fjDiez */
public class NetworksTests {
	
    /** Runs the test suite using the textual runner. */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	public static Test suite() {
		return new JUnit4TestAdapter(NetworksTests.class);
	}

}
