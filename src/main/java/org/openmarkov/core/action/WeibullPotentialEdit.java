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
    private double newRelativeRisk; 
    private List<Variable> newVariables;
    private List<Double> newCoefficients;
    private Variable newTimeVariable;
    private double oldConstant; 
    private double oldShape; 
    private double oldRelativeRisk; 
    private List<Double> oldCoefficients;
    private Variable oldTimeVariable;
    private List<Variable> oldVariables;

    private WeibullPotential potential;
    
    public WeibullPotentialEdit(ProbNet probNet, WeibullPotential potential, List<Variable> variables, double constant,
            double shape, List<Double> coefficients, double relativeRisk, Variable timeVariable) {
        super(probNet);
        this.newConstant = constant;
        this.newShape = shape;
        this.newCoefficients = coefficients;
        this.newRelativeRisk = relativeRisk;
        this.newTimeVariable = timeVariable;
        this.newVariables = variables;
        this.potential = potential;
    }

    @Override
    public void doEdit() throws DoEditException {
        this.oldConstant = potential.getConstant();
        this.oldShape = potential.getShape();
        this.oldCoefficients = potential.getCoefficients();
        this.oldRelativeRisk = potential.getRelativeRisk();
        this.oldTimeVariable = potential.getTimeVariable();
        this.oldVariables = potential.getVariables();
        potential.setConstant(newConstant);
        potential.setVariables(newVariables);
        potential.setShape(newShape);
        potential.setCoefficients(newCoefficients);
        potential.setRelativeRisk(newRelativeRisk);
        potential.setTimeVariable(newTimeVariable);
    }

    @Override
    public void undo() throws CannotUndoException {
        potential.setConstant(oldConstant);
        potential.setShape(oldShape);
        potential.setCoefficients(oldCoefficients);
        potential.setRelativeRisk(oldRelativeRisk);
        potential.setTimeVariable(oldTimeVariable);
        potential.setVariables(oldVariables);
    }

}
