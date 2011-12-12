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
import java.util.List;

import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

public class FormatManager
{
    private static FormatManager instance = null;
    private PluginLoaderIF pluginsLoader; 
    private List<Class<?>> plugins;
    private String roleReader = "Reader";
    private String roleWriter = "Writer";
    

    /**
     * Constructor for FormatManager.
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
   
  
      private final  List<Class<?>> findAllFormatPlugins ()
    {
        try
        {
            FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (FormatType.class);
            return pluginsLoader.loadAllPlugins (filter);          
        }
        catch (Exception e) {}
        return null;
    }

    public ArrayList<String> getItemsByExtension ()
    {
        ArrayList<String> items = new ArrayList<String> ();
        try
        {
            for (Class<?> plugin : plugins) {
                FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
                items.add (lAnnotation.extension());
             }
        }
        catch (Exception e) {}
        
        return items;
    }  
    public ProbNetWriter getProbNetWriter (String extension)
    {
        
        try
        {
            for (Class<?> plugin : plugins) {
                FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
                if (lAnnotation.extension().equals(extension) && lAnnotation.role().equals(roleWriter)){
                	return (ProbNetWriter) plugin.getMethod("getUniqueInstance").invoke(this);
                }
             }
        }
        catch (Exception e) {}
        
        return null;
    } 
    public ProbNetReader getProbNetReader (String extension)
    {
        
        try
        {
            for (Class<?> plugin : plugins) {
                FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
                if (lAnnotation.extension().equals(extension) && lAnnotation.role().equals(roleReader)){
                	return (ProbNetReader) plugin.getMethod("getUniqueInstance").invoke(this);
                }
             }
        }
        catch (Exception e) {}
        
        return null;
    } 
    
  
    
    
   public ArrayList<String> getItemsByRole (String role)
    {
        ArrayList<String> items = new ArrayList<String> ();
        try
        {
            for (Class<?> plugin : plugins) {
                FormatType lAnnotation = plugin.getAnnotation (FormatType.class);
                if (lAnnotation.role().equals(role)){
                	items.add (lAnnotation.extension());
                }
            }
        }
        catch (Exception e) {}
        
        return items;
    }  
    
   
}
