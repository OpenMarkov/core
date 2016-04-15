package org.openmarkov.core.model.network.potential;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.potential.canonical.CanonicalTests;
import org.openmarkov.core.model.network.potential.operation.OperationsTests;
import org.openmarkov.core.model.network.potential.plugin.PluginTests;
import org.openmarkov.core.model.network.potential.treeADD.TreeAddTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CanonicalTests.class,
	OperationsTests.class,
	PluginTests.class,
	TreeAddTests.class,
//	ConditionalGaussianPotentialTest.class,
	InterventionTest.class,
	LinearRegressionPotentialTest.class,
	PotentialTest.class,
	TablePotentialTest.class,
	WeibullHazardPotentialTest.class
})

public class PotentialTests {

}
