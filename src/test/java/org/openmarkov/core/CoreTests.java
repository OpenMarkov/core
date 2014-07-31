package org.openmarkov.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.graph.GraphsTests;
import org.openmarkov.core.model.network.NetworkTests;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	GraphsTests.class,
	NetworkTests.class
})

public class CoreTests {

}
