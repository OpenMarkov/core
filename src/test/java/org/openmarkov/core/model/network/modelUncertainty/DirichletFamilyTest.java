/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author manolo
 * 
 */
public class DirichletFamilyTest extends FamilyDistributionTest {

    @Override
    protected ProbDensityFunctionType getTypeProbDensFunction() {

        return ProbDensityFunctionType.DIRICHLET;
    }

    @Override
    protected List<UncertainValue> initializeListUncertainValues() {

        List<UncertainValue> list;
        // double[] alpha={0.1,0.2,0.3,0.4};
        double[] alpha = { 1.0, 2.0, 3.0, 4.0 };
        list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Double[] aux;
            aux = new Double[1];
            aux[0] = alpha[i];
            list.add(new UncertainValue(getTypeProbDensFunction(), aux));
        }
        return list;
    }

}
