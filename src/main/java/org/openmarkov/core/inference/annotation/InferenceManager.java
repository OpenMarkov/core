package org.openmarkov.core.inference.annotation;

import java.util.List;


import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;
/**
 * This class is the manager of the inference annotations. Detects the plugins with InferenceType 
 * annotations.
 * @see InferenceType
 * @author mpalacios
 * @author myebra
 *
 */

public class InferenceManager {

	private static InferenceManager instance = null;
	/**
	 * The plugin loader
	 */
	private PluginLoaderIF pluginsLoader;
	/**
	 *  The list of plugins detected in the project
	 */
	private List<Class<?>> plugins;

	/**
	 * Gets a InferenceManager instance
	 */
	private InferenceManager ()
	{
		super ();
		this.pluginsLoader = new PluginLoader ();
		this.plugins = findAllInferencePlugins ();
	}    


	public static InferenceManager getInstance()
	{
		if(instance == null)
		{
			instance = new InferenceManager ();
		}
		return instance;
	}


	/**
	 * This method gets all the plugins with InferenceType annotations
	 * @return a list with the plugins detected with InferenceType annotations.
	 */
	private final  List<Class<?>> findAllInferencePlugins ()
	{
		try
		{
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (
					InferenceType.class);
			return pluginsLoader.loadAllPlugins (filter);          
		}
		catch (Exception e) {}
		return null;
	}



	

}
