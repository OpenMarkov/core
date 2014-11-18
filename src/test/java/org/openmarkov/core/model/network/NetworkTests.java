package org.openmarkov.core.model.network;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.constraint.ConstraintsTests;
import org.openmarkov.core.model.network.modelUncertainty.ModelUncertaintyTests;
import org.openmarkov.core.model.network.potential.PotentialTests;
import org.openmarkov.core.model.network.type.TypeTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ConstraintsTests.class,
	ModelUncertaintyTests.class,
	PotentialTests.class,
	TypeTests.class,
	EvidenceCaseTest.class,
	ProbNetOperationsTest.class,
	ProbNetTest.class,
	VariableTest.class,



})


public class NetworkTests {

}
