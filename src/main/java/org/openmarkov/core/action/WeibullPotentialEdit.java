package org.openmarkov.core.action;

import java.util.List;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.WeibullPotential;

@SuppressWarnings("serial")
public class WeibullPotentialEdit extends SimplePNEdit {

    private double newConstant; 
    private double newShape; 
    private List<Variable> newVariables;
    private double[] newCoefficients;
    private double[] newCovarianceMatrix;
    private Variable newTimeVariable;
    private double oldConstant; 
    private double oldShape; 
    private double[] oldCoefficients;
    private Variable oldTimeVariable;
    private List<Variable> oldVariables;
    private double[] oldCovarianceMatrix;

    private WeibullPotential potential;
    
    public WeibullPotentialEdit(ProbNet probNet, WeibullPotential potential, List<Variable> variables, double constant,
            double shape, double[] coefficients, Variable timeVariable, double[] covarianceMatrix) {
        super(probNet);
        this.newConstant = constant;
        this.newShape = shape;
        this.newCoefficients = coefficients;
        this.newCovarianceMatrix = covarianceMatrix;
        this.newTimeVariable = timeVariable;
        this.newVariables = variables;
        this.potential = potential;
    }

    @Override
    public void doEdit() throws DoEditException {
        this.oldConstant = potential.getConstant();
        this.oldShape = potential.getShape();
        this.oldCoefficients = potential.getCoefficients();
        this.oldTimeVariable = potential.getTimeVariable();
        this.oldVariables = potential.getVariables();
        this.oldCovarianceMatrix = potential.getCovarianceMatrix();
        potential.setConstant(newConstant);
        potential.setVariables(newVariables);
        potential.setShape(newShape);
        potential.setCoefficients(newCoefficients);
        potential.setTimeVariable(newTimeVariable);
        potential.setCovarianceMatrix(newCovarianceMatrix);
    }

    @Override
    public void undo() throws CannotUndoException {
        potential.setConstant(oldConstant);
        potential.setShape(oldShape);
        potential.setCoefficients(oldCoefficients);
        potential.setTimeVariable(oldTimeVariable);
        potential.setVariables(oldVariables);
        potential.setCovarianceMatrix(oldCovarianceMatrix);
    }

}
