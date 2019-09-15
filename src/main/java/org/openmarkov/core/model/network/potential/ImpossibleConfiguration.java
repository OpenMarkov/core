package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;

/**
 * Interface implemented by Potentials used in DES models to represent and  calculate Time To Event behaviour
 */
public interface ImpossibleConfiguration {


    public void addImpossibleConfiguration(EvidenceCase configuration);
    public void addImpossibleConfiguration(Configuration configuration);
    public void removeImpossibleConfiguration(EvidenceCase configuration);
    public void removeImpossibleConfiguration(Configuration configuration);

    public boolean hasImpossibleConfiguration();
    public boolean isImpossibleConfiguration(EvidenceCase configuration);
    public boolean isImpossibleConfiguration(Configuration configuration);



    public ArrayList<Configuration> getImpossibleConfigurations();
    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations);
}
