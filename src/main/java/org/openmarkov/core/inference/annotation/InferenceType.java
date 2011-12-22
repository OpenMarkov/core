package org.openmarkov.core.inference.annotation;

/**
 * This class sets the labels for the annotations inference
 * @author mpalacios
 * @author myebra
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface InferenceType {
    /**
     * Gets the name of the class
     * @return The name of the inference algorithm
     */
    String name ();
    
    /**
     * Gets the label identifier of the class
     * @return The identifier of the inference algorithm
     */
    String label ();
}