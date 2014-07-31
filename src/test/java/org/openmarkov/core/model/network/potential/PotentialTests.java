package org.openmarkov.core.model.network.potential;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	InterventionTest.class,
	LinearRegressionPotentialTest.class,
	PotentialTest.class,
	TablePotentialTest.class,
	WeibullHazardPotentialTest.class
})

public class PotentialTests {

}
