package org.openmarkov.core.learning.algorithm.annotation;

import java.util.List;

import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;


public class LearningAlgorithmManager
{  
    private PluginLoaderIF pluginsLoader; 
    
    /**
     * Constructor for LearningAlgoritmManager.
     */
    public LearningAlgorithmManager ()
    {
        super ();
        this.pluginsLoader = new PluginLoader ();
    }

    /**
     * Finds a learning algorithm by name. 
     * @param name the algorithm name.
     * @return a learning algorithm.
     */
    public final Class<?> findLearningAlgorithmByName (String name)
    {
        try
        {
            List<Class<?>> plugins = findAllLearningAlgorimths ();
            for (Class<?> plugin : plugins) {
                LearningAlgorithmType lAnnotation = plugin.getAnnotation (LearningAlgorithmType.class);
                if (lAnnotation.name ().equals (name))
                    return plugin;
            }
        }
        catch (Exception e) {}
        return null;
    }
    
  
    /**
     * Finds all learning algorithms. 
     * @return a list of learning algorithms.
     */
    public final  List<Class<?>> findAllLearningAlgorimths ()
    {
        try
        {
            FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (LearningAlgorithmType.class);
            return pluginsLoader.loadAllPlugins (filter);          
        }
        catch (Exception e) {}
        return null;
    }
   
}

