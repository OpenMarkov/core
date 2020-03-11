package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration of States and Events representing an configuration of Findings in a potential used in a DESNet
 * TODO rename, How describe it?
 * @author cyago
 * @version 1.0 - 01/09/2019
 * @version 1.1 - 04/01/2020 - inherits from EvidenceCase due to we need evidence cases for tables - to be changed with the new version of tables. Inheritance it done because Configuration comparation is needed and EvidenceCase does not have an equal method
 */
public class Configuration extends EvidenceCase{



    /**
     * Creates a Configuration object whose Finding objects are in findings
     * @param findings List of Finding conforming the configuration
     */
    public Configuration(List<Finding> findings) {
        //Create an empty EvidenceCase
        super();
        //Add findings
        for (Finding finding: findings){
            try {
                addFinding(finding);
            } catch (InvalidStateException | IncompatibleEvidenceException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * Creates a Configuration object with the same Finding objects as configuration
     * @param configuration object used to create the new Configuration object
     */
    public Configuration(Configuration configuration) {
        this(configuration.getFindings());
    }

    /**
     * Creates a Configuration object whose Finding objects are in findings
     * @param findings Array of Finding with the findings which conform the new Configuration
     */
    public Configuration(Finding... findings){
        this(Arrays.asList(findings));
    }

    /**
     * Returns a List of Finding sorted alphabetically by its Variable name
     * @return a List of Finding sorted alphabetically by its Variable name
     */
    private List<Finding> getSortedFindings() {
        List<Finding> sortedFindings = getFindings();
        sortedFindings.sort((f1,f2) -> f1.getVariable().getName().compareTo(f2.getVariable().getName()));
        return sortedFindings;
    }


    /**
     * Indicates whether some other object is "equal to" this one. Two Configuration objects are considered equal if they have the same Finding objects.
     * @param obj  the reference object with which to compare
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Configuration))return false;
        Configuration objAsConfiguration =(Configuration) obj;
        List<Finding> objAsConfigurationSortedFindings = objAsConfiguration.getSortedFindings();
        List<Finding> thisSortedFindings = this.getSortedFindings();
        if (thisSortedFindings.size() != objAsConfigurationSortedFindings.size()) return false;
        for (int i = 0; i< thisSortedFindings.size(); i++){
            if (!
                    ( (thisSortedFindings.get(i).getVariable().getBaseName().compareTo(objAsConfigurationSortedFindings.get(i).getVariable().getBaseName()) ==0)
                && (thisSortedFindings.get(i).getState().compareTo(objAsConfigurationSortedFindings.get(i).getState()) ==0) )
                ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new finding with the event Variable variable
     * @param variable to be added to this Configuration
     * @throws NoFindingException exception thrown when the type of variable is not EVEN
     */
    public void addFinding(Variable variable) throws NoFindingException{
        Finding eventFinding =new Finding(variable,0);
        try {
            this.addFinding(eventFinding);
        } catch (InvalidStateException |IncompatibleEvidenceException e) {
            e.printStackTrace();
        }
    }
}
