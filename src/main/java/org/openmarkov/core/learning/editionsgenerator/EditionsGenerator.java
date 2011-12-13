/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.learning.editionsgenerator;


/**
 * This interface defines the basic elements of a generator of possible
 * editions for the interactive learning.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @author ibermejo
 * @version 1.1
 */
public interface EditionsGenerator {

	/**
	 * This method returns the best edition (and its associated score)
	 * that can be done to the network that is being learnt. 
	 * 
	 * @param onlyAllowedEditions If this parameter is true, only those editions
	 * that do not provoke a ConstraintViolationException are returned
	 * @param onlyPositiveEditions If this parameter is true, only those 
	 * editions with a positive associated score are returned.
	 * @return <code>EditAndScorePair</code> with the best edition and its score. 
	 */
    public EditAndScorePair getBest (boolean onlyAllowedEdits,
                                     boolean onlyPositiveEdits);
    
    /**
     * This method returns the next best edition (and its associated score)
     * that can be done to the network that is being learnt. 
     * 
     * @param onlyAllowedEditions If this parameter is true, only those editions
     * that do not provoke a ConstraintViolationException are returned
     * @param onlyPositiveEditions If this parameter is true, only those 
     * editions with a positive associated score are returned.
     * @return <code>EditAndScorePair</code> with the best edition and its score. 
     */
    public EditAndScorePair getNext (boolean onlyAllowedEdits,
                                     boolean onlyPositiveEdits);   
}
