package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.RemoveLinkEdit;

import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.exceptions.NonProjectablePotentialException;
import openmarkov.exceptions.NotEnoughMemoryException;
import openmarkov.exceptions.ProbNodeNotFoundException;
import openmarkov.exceptions.WrongCriterionException;
import openmarkov.graphs.Node;
import openmarkov.networks.ProbNet;
import openmarkov.undo.PNUndoableEditEvent;
import openmarkov.undo.edit.PNEdit;

/** This constraint ensures that the editions done during the learning of a
 * network respect the structure of the model net and the constraints
 * selected by the user. */
public class ModelNetworkConstraint implements PNConstraint {

	// Attributes.
	boolean addLinksAllowed;
	boolean removeLinksAllowed;
	boolean invertLinksAllowed;
	
	ProbNet modelNet;
	
	// Constructor
	public ModelNetworkConstraint(boolean[] modelNetUses, ProbNet modelNet) {
		addLinksAllowed = modelNetUses[2];
		removeLinksAllowed = modelNetUses[3];
		invertLinksAllowed = modelNetUses[4];
		this.modelNet = modelNet.copy();
	}
	
	// Methods	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, NotEnoughMemoryException, 
	NonProjectablePotentialException, WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding doing edition: " +
				event.getEdit().getPresentationName());
		}	
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
		// TODO Auto-generated method stub 
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = new ArrayList<PNEdit>();
		Node source, destination;
		
		try {
			/* Check for prohibited additions. If the link we want to add was
			 * not present in the model net, it is not allowed.
			 */ 
			if (!addLinksAllowed){
				edits = UtilConstraints.getEditsType(event, AddLinkEdit.class);
				for (PNEdit edit : edits) {
						source = modelNet.getProbNode(((AddLinkEdit)edit).
								getVariable1().getName()).getNode();
						destination = modelNet.getProbNode(((AddLinkEdit)edit).
								getVariable2().getName()).getNode();
					if ((modelNet.getGraph().getLink(source, 
							destination, true) == null) &&
							(modelNet.getGraph().getLink(source, 
									destination, true) == null)) { 
						return false;
					}
				} 
			}
			/* Check for prohibited deletions. If the link we want to remove was
			 * in the model net, the elimination is not allowed.
			 */ 
			if (!removeLinksAllowed){
				edits = UtilConstraints.getEditsType(event, RemoveLinkEdit.class);
				for (PNEdit edit : edits) {
						source = modelNet.getProbNode(((RemoveLinkEdit)edit).
								getVariable1().getName()).getNode();
						destination = modelNet.getProbNode(((RemoveLinkEdit)edit).
								getVariable2().getName()).getNode();
					if ((modelNet.getGraph().getLink(source, 
							destination, true) != null) ||
							(modelNet.getGraph().getLink(destination, 
									source, true) != null)) { 
						return false;
					}
				}
			}
			/* Check for prohibited inversions. If the link we want to invert was
			 * in the model net, it is not allowed.
			 */ 
			if (!invertLinksAllowed){
				edits = UtilConstraints.getEditsType(event, InvertLinkEdit.class);
				for (PNEdit edit : edits) {
						source = modelNet.getProbNode(((InvertLinkEdit)edit).
								getVariable1().getName()).getNode();
						destination = modelNet.getProbNode(((InvertLinkEdit)edit).
								getVariable2().getName()).getNode();
					if ((modelNet.getGraph().getLink(source, 
							destination, true) != null)) { 
						return false;
					}
				}
			}
		} catch (ProbNodeNotFoundException e) {
			return(false);
		}
		return true;
	}

}
