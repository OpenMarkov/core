/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.inference.InferenceTests;
import org.openmarkov.core.learning.LearningTests;
import org.openmarkov.core.model.graph.GraphsTests;
import org.openmarkov.core.model.network.NetworksTests;
import org.openmarkov.core.model.network.potential.operation.OperationsTests;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	GraphsTests.class,
    NetworksTests.class,
    OperationsTests.class,
    InferenceTests.class,
    LearningTests.class,
})

/** @author manuel
 * @author fjdiez
 * @version 1.0 manual y fjdiez
 * @version 1.1 jlgozalo Feb2010 - add GuiTest.class
 * 
 * This class is a simple place-holder for the suite annotations, not containing
 * any other functionality as such. The key is in the @RunWith annotation, which
 * tells the JUnit 4 test runner to use the <code>org.junit.runners.Suite</code>
 * class for running this particular class. The @Suite annotation, on the other
 * hand, tells the Suite runner which test classes to include in this suite and
 * in which order. */
public class OpenMarkovTests {

	/** Max error allowed in floating point operations.
	 *  Public scope for use in all tests. */
	public static final double maxError = 0.0001;

}
