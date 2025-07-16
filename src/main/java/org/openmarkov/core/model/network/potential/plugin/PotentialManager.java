/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.plugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PotentialManager {
    
    private Map<String, Class<Potential>> potentials;
    private Map<String, String> potentialFamilies;
    
    /**
     * Constructor for PotentialClassManager.
     */
    @SuppressWarnings("unchecked") public PotentialManager() {
        potentials = new HashMap<>();
        potentialFamilies = new HashMap<>();
        findAllPotentials().forEach(plugin->{
            PotentialType lAnnotation = plugin.getAnnotation(PotentialType.class);
            potentials.put(lAnnotation.name(), plugin);
            potentialFamilies.put(lAnnotation.name(), lAnnotation.family());
        });
    }
    
    public static String getPotentialName(Class<?> clazz) {
        return clazz.getAnnotation(PotentialType.class).name();
    }
    
    public static List<String> getAlternativeNames(Class<?> clazz) {
        return Arrays.asList(clazz.getAnnotation(PotentialType.class).altNames());
    }
    
    /**
     * Returns a potential by name.
     *
     * @param name        the potential's name.
     * @param variables   List of variables
     * @param role        Potential role
     * @param cycleLength Cycle lenghts
     * @return a new Potential instance given the parameters.
     */
    public final Potential getByName(String name, List<Variable> variables, PotentialRole role,
                                     CycleLength... cycleLength) {
        Potential instance = null;
        try {
            Constructor<? extends Potential> constructor;
            
            try {
                if (cycleLength != null && cycleLength.length != 0) {
                    constructor = potentials.get(name).getConstructor(List.class, CycleLength.class);
                    instance = (Potential) constructor.newInstance(variables, cycleLength[0]);
                } else {
                    constructor = potentials.get(name).getConstructor(List.class, PotentialRole.class);
                    instance = (Potential) constructor.newInstance(variables, role);
                }
            } catch (NoSuchMethodException e) {
                constructor = potentials.get(name).getConstructor(List.class);
                instance = constructor.newInstance(variables);
            }
        } catch (NoSuchMethodException e) {
            throw new InvalidParameterException(
                    "\"" + name + "\" does not have a constructor" + " neither that receives a list of variables"
                            + " a list of variables and a potential role.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (instance == null)
            throw new InvalidParameterException();
        return instance;
    }
    
    /**
     * For utility potentials
     *
     * @param name            Name
     * @param utilityVariable Variable
     * @param variables       List of variables
     * @return a potential by name
     */
    public final Potential getByName(String name, Variable utilityVariable, List<Variable> variables) {
        Potential instance = null;
        try {
            Constructor<? extends Potential> constructor;
            
            constructor = potentials.get(name).getConstructor(Variable.class, List.class);
            instance = (Potential) constructor.newInstance(utilityVariable, variables);
        } catch (NoSuchMethodException e) {
            throw new InvalidParameterException("\"" + name + "\" does not have a constructor"
                                                        + "that receives a tuility variable and a list of variables.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (instance == null)
            throw new InvalidParameterException();
        return instance;
    }
    
    /**
     * Returns all potentials' names.
     *
     * @return a list of potentials' names.
     */
    public final Set<String> getAllPotentialsNames() {
        return potentials.keySet();
    }
    
    /**
     * Returns all potentials' names applicable to the given variable list and potential role.
     *
     * @param node Node
     * @return a list of potentials' names.
     */
    public final List<String> getFilteredPotentials(Node node) {
        List<String> filteredPotentials = new ArrayList<>();
        
        Potential potential = node.getPotentials().get(0);
        List<Variable> variables = potential.getVariables();
        PotentialRole potentialRole = potential.getPotentialRole();
        for (String potentialName : potentials.keySet()) {
            Method validateMethod = null;
            try {
                Class<? extends Potential> potentialClass = potentials.get(potentialName);
                validateMethod = potentialClass.getMethod("validate", Node.class, List.class, PotentialRole.class);
                if ((Boolean) validateMethod.invoke(null, node, variables, potentialRole)) {
                    filteredPotentials.add(potentialName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filteredPotentials;
    }
    
    /**
     * Returns the family of the given potential type
     *
     * @param name Name of the potentials family
     * @return the family of the given potential type
     */
    public String getPotentialsFamily(String name) {
        return potentialFamilies.get(name);
    }
    
    /**
     * Finds all learning algorithms.
     *
     * @return a list of learning algorithms.
     */
    private static @NotNull Stream<Class<Potential>> findAllPotentials() {
        return PluginSearch.init()
                           .annotatedWith(PotentialType.class)
                           .childrenOf(Potential.class)
                           .stream();
    }
    
}

