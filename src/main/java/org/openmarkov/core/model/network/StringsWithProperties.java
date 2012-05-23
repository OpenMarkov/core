package org.openmarkov.core.model.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** @author marias */
public class StringsWithProperties {

	// Attributes
	private HashMap<String, AdditionalProperties> stringsWithProperties;
	
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
	/** @return A string property if it exists or <code>null</code> otherwise. */
	public Object get(String string, String propertyName) {
		Object propertyValue = null;
		AdditionalProperties properties = stringsWithProperties.get(string);
		if (properties != null) {
			propertyValue = properties.get(propertyName);
		}
		return propertyValue;
	}
	
	/** @return All the strings in a <code>Set</code> of <code>String</code>. */
	public Set<String> getNames() {
		return stringsWithProperties.keySet();
	}
	
	/** @return All the properties of a given <code>String</code>, or <code>null</code> 
	 * if the string does not exists. <code>AdditionalProperties</code>. 
	 * @param string. <code>String</code> */
	public AdditionalProperties getProperties(String string) {
		return stringsWithProperties.get(string);
	}

	/** @param key. <code>String</code> */
	public void put(String key) {
		AdditionalProperties properties = stringsWithProperties.get(key);
		if (properties == null) {
			properties = new AdditionalProperties();
			stringsWithProperties.put(key, null);
		}
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
	
	/** @param key. <code>String</code>
	 * @param properties. <code>AdditionalProperties</code> */
	public void put(String key, AdditionalProperties properties) {
		if (properties == null) {
			properties = new AdditionalProperties();
			stringsWithProperties.put(key, properties);
		}
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

	public boolean isEmpty() {
		return stringsWithProperties.isEmpty();
	}
	
}
