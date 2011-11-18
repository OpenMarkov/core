
package org.openmarkov.core.model.network.type;


public class InfluenceDiagramType extends NetworkType
{
    private static InfluenceDiagramType instance = null;

    // Constructor
    private InfluenceDiagramType ()
    {
        super();
    }

    // Methods
    public static InfluenceDiagramType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new InfluenceDiagramType ();
        }
        return instance;
    }
}
