package org.openmarkov.core.inference;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.canonical.CanonicalTests;

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
