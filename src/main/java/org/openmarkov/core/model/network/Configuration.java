package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of States and Events representing an impossible configuration in a potential used in a DESNet
 * TODO rename
 * @author cyago
 * @verion 1.0 20190901 01/09/2019
 */

public class Configuration {

    private ArrayList<Finding> configuration;


    public Configuration(List<Finding> configuration) {
        this.configuration = new ArrayList<>(configuration);
        sort();

    }

    /**
     *
     */
    public Configuration(EvidenceCase configuration){
        this(configuration.getFindings());
    }


    public Configuration(Finding... findings){
        this.configuration = new ArrayList<Finding>();
        for (Finding finding: findings){
            configuration.add(finding);
        }
        sort();
    }


    private void  sort(){
        configuration.sort((finding1, finding2) ->
                finding1.getVariable().getName().compareTo(finding2.getVariable().getName())
        );
    }


    public EvidenceCase convertToEvidenceCase(){
        EvidenceCase ec = new EvidenceCase();
        for (Finding finding:configuration){
            try {
                ec.addFinding(finding);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            } catch (IncompatibleEvidenceException e) {
                e.printStackTrace();
            }
        }
        return ec;
    }


    /**
     * Two configurations are equal if they have the same findings
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Configuration))return false;
        Configuration objIC =(Configuration) obj;
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

    /**
     * Remove the finding with variable
     * @param variable the Variable whose Finding is removed
     */
    public void remove(Variable variable) {
        configuration.removeIf( f -> f.getVariable().getName().equals(variable.getName()) );
    }

    /**
     * Adds a Finding to configuration
     */
    public void add (Finding f){
        configuration.add(f);
    }
}
