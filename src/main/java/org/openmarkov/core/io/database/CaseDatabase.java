/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.io.database;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class CaseDatabase
{
    private List<Variable> variables;
    private int[][] cases;

    /**
     * Constructor for CaseDatabase.
     * @param probNet
     * @param cases
     */
    public CaseDatabase (List<Variable> variables, int[][] cases)
    {
        super ();
        this.variables = new ArrayList<> (variables);
        this.cases = cases;
    }
        
    /**
     * Returns the cases.
     * @return the cases.
     */
    public int[][] getCases ()
    {
        return cases;
    }

    /**
     * Returns the variables.
     * @return the variables.
     */
    public List<Variable> getVariables ()
    {
        return variables;
    }

}
