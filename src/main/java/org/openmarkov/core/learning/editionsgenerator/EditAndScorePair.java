package org.openmarkov.core.learning.editionsgenerator;

import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.exception.ConstraintViolationException;

/** An <code>EditAndScorePair</code> stores a <code>PNEdit</code> and the
 * increment of score associated to this edition. Also it stores a pointer
 * to the constraint violated by this edition.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since Carmen 1.0 */
public class EditAndScorePair {

    protected BaseLinkEdit edition;
    
    protected double score;
    
    protected Exception violatedConstraint = null;
    
    public EditAndScorePair(BaseLinkEdit edition, double score){
        this.edition = edition; 
        this.score = score;
    }
    
    public BaseLinkEdit getEdition(){
        return edition;
    }
    
    public double getScore(){
        return score;
    }
    
    public Exception getViolatedConstraint(){
    	return violatedConstraint;
    }
    
    public void setViolatedConstraint(ConstraintViolationException violatedConstraint){
    	this.violatedConstraint = violatedConstraint;
    }
    
    public boolean isAllowed()
    {
    	return violatedConstraint == null;
    }
    
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if((obj == null) || (obj.getClass() != this.getClass()))
            return false;
        return (this.edition.equals(((EditAndScorePair)obj).edition));
    }
    
    public String toString()
    {
        return new StringBuilder().append (edition.toString () + " " + score).toString (); 
    }
}
