package org.openmarkov.core.action;

import java.util.List;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.WeibullPotential;

@SuppressWarnings("serial")
public class WeibullPotentialEdit extends SimplePNEdit {

    private double newConstant; 
    private double newGamma; 
    private List<Double> newCoefficients;
    private double oldConstant; 
    private double oldGamma; 
    private List<Double> oldCoefficients;

    private WeibullPotential potential;
    
    public WeibullPotentialEdit(ProbNet probNet, WeibullPotential potential, double constant,
            double gamma, List<Double> coefficients) {
        super(probNet);
        this.newConstant = constant;
        this.newGamma = gamma;
        this.newCoefficients = coefficients;
        this.potential = potential;
    }

    @Override
    public void doEdit() throws DoEditException {
        this.oldConstant = potential.getConstant();
        this.oldGamma = potential.getGamma();
        this.oldCoefficients = potential.getCoefficients();
        potential.setConstant(newConstant);
        potential.setGamma(newGamma);
        potential.setCoefficients(newCoefficients);
    }

    @Override
    public void undo() throws CannotUndoException {
        potential.setConstant(oldConstant);
        potential.setGamma(oldGamma);
        potential.setCoefficients(oldCoefficients);
    }

}
