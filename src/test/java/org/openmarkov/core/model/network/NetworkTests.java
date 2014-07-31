package org.openmarkov.core.model.network;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.potential.PotentialTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	EvidenceCaseTest.class,
	ProbNetOperationsTest.class,
	ProbNetTest.class,
	VariableTest.class,
	PotentialTests.class
})


public class NetworkTests {

}
