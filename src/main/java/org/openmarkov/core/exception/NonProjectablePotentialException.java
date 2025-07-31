/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import net.sourceforge.jeval.EvaluationException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.List;

/**
 * Thrown when the {@code Potential} cannot be projected into a set of
 * {@code TablePotential}s given the evidence supplied.
 */
public abstract sealed class NonProjectablePotentialException extends BundledOpenMarkovException {
    
    public static final class SuperValueMustBeSumOrProduct extends NonProjectablePotentialException {
        public SuperValueMustBeSumOrProduct(Potential potential) {
            this.potential = potential;
        }
        
        private final Potential potential;
    }
    
    public static final class PotentialCannotBeConvertedToATable extends NonProjectablePotentialException {
        public PotentialCannotBeConvertedToATable(Potential potential) {
            this.potential = potential;
        }
        
        private final Potential potential;
    }
    
    public static final class PotentialCannotBeConvertedToATableDueToVariable extends NonProjectablePotentialException {
        public PotentialCannotBeConvertedToATableDueToVariable(Potential potential, Variable variable) {
            this.potential = potential;
            this.variable = variable;
        }
        
        private final Potential potential;
        private final Variable variable;
    }
    
    public static final class MissingVariableInEvidence extends NonProjectablePotentialException {
        public MissingVariableInEvidence(Variable variable, EvidenceCase evidenceCase) {
            this.variable = variable;
            this.evidenceCase = evidenceCase;
        }
        
        private final Variable variable;
        private final EvidenceCase evidenceCase;
    }
    
    public static final class MissingEvidenceInVariable extends NonProjectablePotentialException {
        public MissingEvidenceInVariable(Potential potential, Variable timeVariable) {
            this.potential = potential;
            this.timeVariable = timeVariable;
        }
        
        private final Potential potential;
        private final Variable timeVariable;
    }
    
    public static final class TopVariableNotInDomain extends NonProjectablePotentialException {
        public TopVariableNotInDomain(TreeADDPotential treeADDPotential, List<TreeADDBranch> branches) {
            this.treeADDPotential = treeADDPotential;
            this.branches = branches;
        }
        
        private final TreeADDPotential treeADDPotential;
        private final List<TreeADDBranch> branches;
    }
    
    public static final class CannotEvaluate extends NonProjectablePotentialException {
        public CannotEvaluate(String elementToEvaluate, EvaluationException evaluationException) {
            this.elementToEvaluate = elementToEvaluate;
            this.evaluationException = evaluationException;
        }
        
        private final String elementToEvaluate;
        private final EvaluationException evaluationException;
    }
}
