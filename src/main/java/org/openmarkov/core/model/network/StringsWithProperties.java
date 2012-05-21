package org.openmarkov.core.model.network;

import java.util.Collection;
import java.util.HashMap;

/** @author marias */
public class StringsWithProperties {

	// Attributes
	public HashMap<String, AdditionalProperties> stringsWithProperties;
	
	// Constructors
	public StringsWithProperties() {
		stringsWithProperties = new HashMap<String, AdditionalProperties>();
	}
	
	/** Constructor that receives a collection of strings (without properties) 
	 * @param strings. <code>Collection</code> of <code>String</code>s */
	public StringsWithProperties(Collection<String> strings) {
		stringsWithProperties = new HashMap<String, AdditionalProperties>();
		for (String string : strings) {
			stringsWithProperties.put(string, null);
		}
	}
	
	// Methods
	/** @return The property value stored in the string <code>key</code> with the property name 
	 * <code>propertyName</code> or <code>null</code> if it does not exists. */
	public Object get(String key, String propertyName) {
		Object propertyValue = null;
		AdditionalProperties properties = stringsWithProperties.get(key);
		if (properties != null) {
			propertyValue = properties.get(propertyName);
		}
		return propertyValue;
	}
	
	/** @param key. <code>String</code>
	 * @param propertyName. <code>String</code>
	 * @param propertyValue. <code>String</code> */
	public void put(String key, String propertyName, String propertyValue) {
		AdditionalProperties properties = stringsWithProperties.get(key);
		if (properties == null) {
			properties = new AdditionalProperties();
			stringsWithProperties.put(key, properties);
		}
		properties.put(propertyName, propertyValue);
	}
	
	/** @return The object stored with <code>key</code> or <code>null</code> if
	 * it does not exists. */
	public Object remove(String key, String propertyName) {
		Object removedObject = null;
		AdditionalProperties properties = stringsWithProperties.get(key);
		if (properties != null) {
			removedObject = properties.remove(propertyName);
			if (properties.size() == 0) {
				stringsWithProperties.remove(properties);
			}
		}
		return removedObject;
	}

}
