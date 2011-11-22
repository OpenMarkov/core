
package org.openmarkov.core.model.network.type;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;

public class NetworkTypeTests
{
    @Test (expected=ConstraintViolationException.class) 
    public void testConstraints () throws ConstraintViolationException
    {
        ProbNet probNet = new ProbNet ();
        probNet.addConstraint (new OnlyUndirectedLinks());

    }
}
