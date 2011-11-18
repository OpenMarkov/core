package org.openmarkov.core.model.network.constraint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

public class ConstraintManager
{
    private static ConstraintManager instance;
    private PluginLoaderIF pluginsLoader;

    /**
     * Constructor for LearningAlgoritmManager.
     */
    private ConstraintManager ()
    {
        super ();
        this.pluginsLoader = new PluginLoader ();
    }
    
    // Methods
    /**
     * Singleton pattern.
     * @return The unique instance.
     */
    public static ConstraintManager getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new ConstraintManager ();
        }
        return instance;
    }
    
    /**
     * Generates the minimal (i.e. not including optional constraints)
     * constraint list given the network type and the Constraints annotated as
     * such.
     * @param type of the network the list is being generated for.
     * @return a minimal list of constraint.
     */
    public final ArrayList<PNConstraint> buildConstraintList (NetworkType type, boolean includeOptionals)
    {
        // Init the list with those constraints that have the default value set to YES 
        ArrayList<PNConstraint> constraints = new  ArrayList<PNConstraint> ();
        try
        {
            List<Class<?>> plugins = findAllConstraints ();
            for (Class<?> plugin : plugins)
            {
                Constraint lAnnotation = plugin.getAnnotation (Constraint.class);
                if (lAnnotation.defaultBehavior ().equals (ConstraintBehavior.YES)
                    || (includeOptionals && lAnnotation.defaultBehavior ().equals (ConstraintBehavior.OPTIONAL)))
                {
                    constraints.add ((PNConstraint) plugin.newInstance ());
                }
            }
        }
        catch (Exception e)
        {
        }
        

        // Overwrite the list with the constraints specified in the corresponding network type
        HashMap<PNConstraint, ConstraintBehavior> hashMap =  type.getConstraints ();
        for(PNConstraint constraint: hashMap.keySet ())
        {
            if(hashMap.get (constraint) == ConstraintBehavior.YES && !constraints.contains (constraint))
            {
                constraints.add (constraint);
            }else if (hashMap.get (constraint) == ConstraintBehavior.NO && constraints.contains (constraint))
            {
                constraints.remove (constraint);
            }
        }
        return constraints;
       
    }
    
    public final ArrayList<PNConstraint> buildConstraintList (NetworkType type)
    {
        return buildConstraintList (type, false);
    }
    
    public final List<Class<?>> findAllConstraints ()
    {
        try
        {
            FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy (Constraint.class);
            return pluginsLoader.loadAllPlugins (filter);          
        }
        catch (Exception e) {}
        return null;
    }    
}
