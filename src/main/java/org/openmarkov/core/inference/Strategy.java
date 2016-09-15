/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

public class Strategy {

    Hashtable<Variable, Policy> strategy;

    public class Policy {

        GTablePotential policyPotential;

        /**
         * @param decisionVariable
         * @param utilities
         */
        public Policy(Variable decisionVariable, TablePotential utilities) {
            this();
            policyPotential = (GTablePotential) 
            		DiscretePotentialOperations.maximize(utilities,
                    decisionVariable)[1];
        }

        public Policy() {
        	policyPotential = null;
        }

        /**
         * @return Potential that describes the policy. <code>GTablePotential</code>
         */
        public GTablePotential getPotential() {
            return policyPotential;
        }

        /**
         * @return List of policy variables
         */
        public List<Variable> getDomain() {
            return policyPotential.getVariables();
        }

    }

    // Constructors
    public Strategy() {
        strategy = new Hashtable<>();
    }

    /**
     * Constructs a strategy by maximizing over the utility tables
     * @param stratUtil
     */
    public Strategy(StrategyUtilities stratUtil) {
        this();
        Set<Variable> decisions = stratUtil.getUtilities().keySet();

        for (Variable dec : decisions) {
            setPolicy(dec, new Policy(dec, stratUtil.getUtilities(dec)));
        }

    }

    // Methods
    /**
     * @param decisionVariable
     * @param policy
     */
    private void setPolicy(Variable decisionVariable, Policy policy) {
        strategy.put(decisionVariable, policy);

    }

    /**
     * @param varDecision
     * @return List of policy variables
     */
    public List<Variable> getDomainOfPolicy(Variable varDecision) {
        return getPolicy(varDecision).getDomain();
    }

    /**
     * @param varDecision
     * @return <code>Policy</code>
     */
    public Policy getPolicy(Variable varDecision) {
        return strategy.get(varDecision);
    }

}
