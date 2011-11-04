package org.openmarkov.core.learning.metric.annotation;

import java.util.List;

import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

public class MetricManager
{
    private PluginLoaderIF pluginsLoader; 
    
    /**
     * Constructor for MetricManager.
     */
    public MetricManager ()
    {
        super ();
        this.pluginsLoader = new PluginLoader ();
    }

    /**
     * Finds a learning algorithm by name. 
     * @param name the algorithm name.
     * @return a learning algorithm.
     */
    public final Class<?> findMetricByName (String name)
    {
        try
        {
            List<Class<?>> plugins = findAllMetrics ();
            for (Class<?> plugin : plugins) {
                MetricType lAnnotation = plugin.getAnnotation (MetricType.class);
                if (lAnnotation.name ().equals (name))
                    return plugin;
            }
        }
        catch (Exception e) {}
        return null;
    }
    
  
    /**
     * Finds all metrics. 
     * @return a list of metrics.
     */
    public final  List<Class<?>> findAllMetrics ()
    {
        try
        {
            FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (MetricType.class);
            return pluginsLoader.loadAllPlugins (filter);          
        }
        catch (Exception e) {}
        return null;
    }
   
}

