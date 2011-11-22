package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;

public interface Checkable{

   /** @param probNet. <code>ProbNet</code>
    * @return <code>true</code> if the <code>probNet</code> fulfills the 
    * condition. */
   public boolean checkProbNet(ProbNet probNet);
   
   /** Make sure all editions of the event fulfill the condition.
    * @param probNet. <code>ProbNet</code>
    * @param event <code>UndoableEditEvent</code>
    * @return <code>true</code> if the <code>ProbNet</code> will fulfill certain
    *  condition after applying the <code>event</code> in a 
    *  <code>ProbNet</code> that previously fulfilled the constraint. 
    * @throws NotEnoughMemoryException 
    * @throws WrongCriterionException 
    * @throws NonProjectablePotentialException */
   public boolean checkEvent(UndoableEditEvent event) 
   throws NotEnoughMemoryException, NonProjectablePotentialException, 
   WrongCriterionException;
   
}
