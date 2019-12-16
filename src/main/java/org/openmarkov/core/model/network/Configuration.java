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

    private ArrayList<Finding> findings;


    public Configuration(List<Finding> findings) {
        this.findings = new ArrayList<>(findings);
        sort();

    }

    /**
     *
     */
    public Configuration(EvidenceCase configuration){
        this(configuration.getFindings());
    }


    public Configuration(Finding... findings){
        this.findings = new ArrayList<Finding>();
        for (Finding finding: findings){
            if (finding !=null)
            this.findings.add(finding);
        }
        sort();
    }

    public Configuration(Configuration configuration) {
       this(configuration.getFindings());
    }


    private void  sort(){
        findings.sort((finding1, finding2) ->
                finding1.getVariable().getName().compareTo(finding2.getVariable().getName())
        );
    }


    public EvidenceCase convertToEvidenceCase(){
        EvidenceCase ec = new EvidenceCase();
        for (Finding finding: findings){
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
        ArrayList<Finding> configurationObjIC = objIC.getFindings();
        if (findings.size() != configurationObjIC.size()) return false;
        for (int i = 0; i< findings.size(); i++){
            if (!
                    ( (findings.get(i).getVariable().getBaseName().compareTo(configurationObjIC.get(i).getVariable().getBaseName()) ==0)
                && (findings.get(i).getState().compareTo(configurationObjIC.get(i).getState()) ==0) )
                ) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Finding> getFindings() {
        return findings;
    }

    public void setFindings(ArrayList<Finding> findings) {
        this.findings = findings;
    }

    /**
     * Remove the finding with variable
     * @param variable the Variable whose Finding is removed
     */
    public void remove(Variable variable) {
        findings.removeIf(f -> f.getVariable().getName().equals(variable.getName()) );
    }

    /**
     * Adds a Finding to configuration
     */
    public void add (Finding f){
        findings.add(f);
    }
}
