package org.openmarkov.core.model.network;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Configuration of States and Events representing an impossible configuration in a potential used in a DESNet
 * TODO rename
 * @author cyago
 * @verion 1.0 20190901 01/09/2019
 */

public class ImpossibleConfiguration {

    private ArrayList<Finding> configuration;


    public ImpossibleConfiguration(ArrayList<Finding> configuration) {
            configuration = new ArrayList<Finding>();
    }

    /**
     *
     */
    public ImpossibleConfiguration(EvidenceCase impossibleConfiguration){
        List<Finding> findings = impossibleConfiguration.getFindings();
        List<Variable> variables = impossibleConfiguration.getVariables();
        configuration = new ArrayList<>(findings);

        configuration.sort((finding1, finding2) ->
                finding1.getVariable().getBaseName().compareTo(finding2.getVariable().getBaseName())
        );
    }

    /**
     * Two configurations are equal if they have the same findings
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof ImpossibleConfiguration))return false;
        ImpossibleConfiguration objIC =(ImpossibleConfiguration) obj;
        ArrayList<Finding> configurationObjIC = objIC.getConfiguration();
        if (configuration.size() != configurationObjIC.size()) return false;
        for (int i=0; i<configuration.size();i++){
            if (!
                    ( (configuration.get(i).getVariable().getBaseName().compareTo(configurationObjIC.get(i).getVariable().getBaseName()) ==0)
                && (configuration.get(i).getState().compareTo(configurationObjIC.get(i).getState()) ==0) )
                ) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Finding> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ArrayList<Finding> configuration) {
        this.configuration = configuration;
    }
}
