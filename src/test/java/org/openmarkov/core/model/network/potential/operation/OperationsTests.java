/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.operation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UtilTest.class,
	AuxiliaryOperationsTest.class,
    DiscretePotentialOperationsTest.class,
    LinkRestrictionPotentialOperationsTest.class
})
        
/** @author manuel */
public class OperationsTests {

}

