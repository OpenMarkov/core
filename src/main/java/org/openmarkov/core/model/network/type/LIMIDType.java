package org.openmarkov.core.model.network.type;


public class LIMIDType extends NetworkType
{
    private static LIMIDType instance = null;

    // Constructor
    private LIMIDType ()
    {
        super();
    }

    // Methods
    public static LIMIDType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new LIMIDType ();
        }
        return instance;
    }

    /** @return String "LIMID" */
    public String toString() {
    	return "LIMID";
    }
    
}
