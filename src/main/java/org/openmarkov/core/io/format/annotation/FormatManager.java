/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.format.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

/**
 * This class is the manager of the format annotations. Detects the plugins with FormatType 
 * annotations.
 * @see FormatType
 * @author mpalacios
 *
 */
public class FormatManager
{
	private static FormatManager instance = null;
	/**
	 * The plugin loader
	 */
	private PluginLoaderIF pluginsLoader;
	/**
	 *  The list of plugins detected in the project
	 */
	private List<Class<?>> plugins;
	/**
	 * The Reader role
	 */
	private String roleReader = "Reader";
	/**
	 * The writer role
	 */
	private String roleWriter = "Writer";

	/**
	 * Gets a FormatManager instance
	 */
	private FormatManager ()
	{
		super ();
		this.pluginsLoader = new PluginLoader ();
		this.plugins = findAllFormatPlugins ();
	}    


	public static FormatManager getInstance()
	{
		if(instance == null)
		{
			instance = new FormatManager ();
		}
		return instance;
	}


	/**
	 * This method gets all the plugins with FormatType annotations
	 * @return a list with the plugins detected with FormatType annotations.
	 */
	private final  List<Class<?>> findAllFormatPlugins ()
	{
		try
		{
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (
					FormatType.class);
			return pluginsLoader.loadAllPlugins (filter);          
		}
		catch (Exception e) {}
		return null;
	}

	/**
	 * Gets the plugin with the "Writer" role and the extension 
	 * @param extension the extension required
	 * @return a probNetWriter object
	 */
	public ProbNetWriter getProbNetWriter (String extension)
	{

		try
		{
			for (Class<?> plugin : plugins) {
				FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
				if (lAnnotation.extension().equals(extension) && lAnnotation.role().
						equals(roleWriter)){
					return (ProbNetWriter) plugin.getMethod("getUniqueInstance").invoke(this);
				}
			}
		}
		catch (Exception e) {}

		return null;
	} 
	/**
	 * Gets the plugin with the "Reader" role and the extension 
	 * @param extension the extension required
	 * @return a probNetReader object
	 */
	public ProbNetReader getProbNetReader (String extension)
	{

		try
		{
			for (Class<?> plugin : plugins) {
				FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
				if (lAnnotation.extension().equals(extension) && lAnnotation.role().
						equals(roleReader)){
					return (ProbNetReader) plugin.getMethod("getUniqueInstance").invoke(this);
				}
			}
		}
		catch (Exception e) {}

		return null;
	} 


	/**
	 * Gets the all the plugins with the role specified  
	 * @param role the role of the plugins to search
	 * @return the plugins founded with the role specified
	 */

	public HashMap<String, String> getItemsByRole (String role)
	{
		HashMap<String, String> items = new HashMap<String, String> ();
		try
		{
			for (Class<?> plugin : plugins) {
				FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
				if (lAnnotation.role().equals(role)){
					items.put (lAnnotation.description(), lAnnotation.extension());
				}
			}
		}
		catch (Exception e) {}

		return items;
	}  


}
