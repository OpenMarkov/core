package org.openmarkov.core.learning.preprocess.exception;

/**
 * Thrown when the minimum is under the left limit of the first interval, 
 * or the maximum is greater than the right limit of the last interval, 
 * @author Iñigo
 *
 */
@SuppressWarnings("serial")
public class WrongDiscretizationLimitException extends Exception {

}
