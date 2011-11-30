package org.openmarkov.core.learning.algorithm.annotation;

import java.lang.annotation.AnnotationFormatError;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.learning.algorithm.LearningAlgorithm;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;


public class LearningAlgorithmManager
{  
    private PluginLoaderIF pluginsLoader;
    private HashMap<String, Class<? extends LearningAlgorithm>> learningAlgorithms;
    
    /**
     * Constructor for LearningAlgoritmManager.
     */
    @SuppressWarnings("unchecked")
    public LearningAlgorithmManager ()
    {
        super ();
        this.pluginsLoader = new PluginLoader ();
        learningAlgorithms = new HashMap<String, Class<? extends LearningAlgorithm>> ();
        
        for (Class<?> plugin : findAllLearningAlgorithms ())
        {
            LearningAlgorithmType lAnnotation = plugin.getAnnotation (LearningAlgorithmType.class);
            if (LearningAlgorithm.class.isAssignableFrom (plugin))
            {
                learningAlgorithms.put (lAnnotation.name (), (Class<? extends LearningAlgorithm>)plugin);
            }
            else
            {
                throw new AnnotationFormatError ("LearningAlgorithmType annotation must be in a class that extends LearningAlgorithm");
            }
        }  
    }
    /**
     * Returns a learning algorithm by name. 
     * @param name the algorithm name.
     * @return a learning algorithm.
     */
    public final LearningAlgorithm getByName (String name, HashMap<Class<?>, Object> parameters)
    {
        LearningAlgorithm instance = null;
        try
        {
            // TODO Make this dynamic
            instance = learningAlgorithms.get (name).getConstructor ().newInstance ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return instance;
    }
    
    /**
     * Returns all learning algorithm names. 
     * @return a list of learning algorithms.
     */
    public final  Set<String> getLearningAlgorithmNames ()
    {
        return learningAlgorithms.keySet ();
    }    
    
  
    /**
     * Finds all learning algorithms. 
     * @return a list of learning algorithms.
     */
    private final  List<Class<?>> findAllLearningAlgorithms ()
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

