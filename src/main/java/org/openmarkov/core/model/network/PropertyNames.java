package org.openmarkov.core.model.network;

/** This interface defines (in alphabetical order) a set of tokens used 
 *  adittionalProperties. */
public interface PropertyNames {

enum netPropertyNames {DEFAULT_STATES, COMMENT, NAME, TYPE, 
	VARIABLES_CONSTRAINT, OTHER_PROPERTIES, DEFAULT_PARTITIONEDINTERVAL};
	
enum nodePropertyNames {NAME, STATES, COMMENT, PRECISION, RELEVANCE, TYPE, 
	PARTITIONEDINTERVAL, PURPOSE, MODEL_TYPE, RELATION_TYPE, CANONICAL_PARAMETERS,
	COMPOUSE_VALUES, AS_VALUES, ALL_PARAMETERS};
enum stateActions {ADD, REMOVE, RENAME, UP, DOWN}

}
