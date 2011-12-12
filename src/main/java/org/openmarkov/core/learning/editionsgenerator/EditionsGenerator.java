/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.learning.editionsgenerator;

import java.util.ArrayList;

/**
 * This interface defines the basic elements of a generator of possible
 * editions for the interactive learning.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 */
public interface EditionsGenerator {

	/**
	 * This method returns the best n editions (and their associated scores)
	 * that can be done to the network that is being learnt. 
	 * 
	 * @param probNet
	 * @param cases
	 * @param numEdits Number of editions that should be returned. 
	 * @param onlyAllowedEditions If this parameter is true, only those editions
	 * that do not provoke a modelNetworkConstraintViolation are returned
	 * @param onlyPositiveEditions If this parameter is true, only those 
	 * editions with a positive associated score are returned.
	 * @param reset If this parameter is true, the results provided will be  
	 * computed again forgetting previous calculations
	 * @return <code>ArrayList</code> of <code>EditAndScorePair</code> with the
	 * editions and scores requested. 
	 */
    public ArrayList<EditAndScorePair> getBestEditions (int numEdits,
                                                        boolean onlyAllowedEdits,
                                                        boolean onlyPositiveEdits,
                                                        boolean reset);	
}
