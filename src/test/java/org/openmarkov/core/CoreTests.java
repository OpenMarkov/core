package org.openmarkov.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.action.ActionTests;
import org.openmarkov.core.dt.DtTests;
import org.openmarkov.core.model.graph.GraphsTests;
import org.openmarkov.core.model.network.NetworkTests;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	GraphsTests.class,
	NetworkTests.class,
	ActionTests.class,
	DtTests.class
})

public class CoreTests {

}
