package org.openmarkov.core.learning;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.learning.algorithm.LearningAlgorithmsTest;
import org.openmarkov.core.learning.metric.MetricsTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	// TODO Corregir error en la prueba de CSVDataBaseIOTest y añadirlo
//	DataBaseIOTests.class, 	
    MetricsTest.class,
    LearningAlgorithmsTest.class
        })
        
        
/** @author joliva 
 * @author manuel
 * @author fjdiez */
public class LearningTests {

	/** Runs the test suite using the textual runner. */
	public static void main(String[] args) {
	    junit.textui.TestRunner.run(suite());
	}
	
	public static Test suite() {
		return new JUnit4TestAdapter(LearningTests.class);
	}
}
