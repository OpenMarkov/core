
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;
import org.openmarkov.core.action.base.StateAction;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class DecisionCriteriaEdit extends SimplePNEdit {
	private StateAction stateAction;
	private List<Criterion> lastCriteria;
	private Criterion modifiedCriterion;
    private final @Nullable String newName;

	public DecisionCriteriaEdit(ProbNet probnet, StateAction stateAction, Criterion modifiedCriterion, String newName) {
		super(probnet);
		this.modifiedCriterion = modifiedCriterion;
        
        if (stateAction == StateAction.ADD) {
			this.newName = modifiedCriterion.getCriterionName();
        } else if (stateAction == StateAction.RENAME) {
			this.newName = newName;
        } else {
            this.newName = null;
        }
		this.stateAction = stateAction;
		this.lastCriteria = new ArrayList<>(probnet.getDecisionCriteria());
	}
    
    @Override public void checkConstraintsWillBeMet() throws ConstraintViolatedException {
        if (probNet.getConstraintOfClass(OnlyTemporalVariables.class) instanceof OnlyTemporalVariables constraint) {
            switch (this.stateAction) {
                case REMOVE, MODIFY_VALUE_INTERVAL, MODIFY_DELIMITER_INTERVAL, DOWN, UP -> {
                }
                case ADD, RENAME -> {
                    String name = this.newName.trim().toLowerCase();
                    if (name.isEmpty()) {
                        throw new ConstraintViolatedException(constraint);
                    }
                    for (Criterion criterion : this.lastCriteria) {
                        if (criterion.getCriterionName().trim().toLowerCase().equals(name)) {
                            throw new ConstraintViolatedException(constraint);
                        }
                    }
                }
            }
        }
    }
    
    @Override public void doEdit() {
		List<Criterion> criteria = probNet.getDecisionCriteria();
		switch (stateAction) {
		case ADD:
			criteria.add(modifiedCriterion);
			break;
		case REMOVE:

			criteria.remove(modifiedCriterion);
            
            if (criteria.isEmpty()) {
            }
			break;
		case DOWN:

			int criterionIndex = criteria.indexOf(modifiedCriterion);
			Criterion swapDown = criteria.get(criterionIndex);
			criteria.set(criterionIndex, criteria.get(criterionIndex + 1));
			criteria.set(criterionIndex + 1, swapDown);

			break;
		case UP:
			criterionIndex = criteria.indexOf(modifiedCriterion);

			Criterion swapUp = criteria.get(criterionIndex);
			criteria.set(criterionIndex, criteria.get(criterionIndex - 1));
			criteria.set(criterionIndex - 1, swapUp);

			break;
		case RENAME:
			String oldName = modifiedCriterion.getCriterionName();
			modifiedCriterion.setCriterionName(newName);
			break;
		default:
			break;
		}
	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	public String getNewName() {
		return newName;
	}

	public StateAction getStateAction() {
		return stateAction;
	}

	public List<Criterion> getLastCriteria() {
		return lastCriteria;
	}

	@Override public void undo() {
		super.undo();
		probNet.setDecisionCriteria(lastCriteria);

	}
}
