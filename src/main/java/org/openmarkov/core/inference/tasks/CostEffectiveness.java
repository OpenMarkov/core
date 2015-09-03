package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 13/07/2015.
 */
public abstract class CostEffectiveness extends Task {
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public CostEffectiveness(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    public boolean checkNetworkConsistency() throws NotEvaluableNetworkException {
        if (!isEvaluable(probNet)) {
            throw new NotEvaluableNetworkException("Not evaluable (CE)");
        } else {
            return true;
        }
    }

    public boolean checkEvidenceConsistency() {
        return true;
    }

    public boolean checkPoliciesConsistency() {
        return true;
    }

    /**
     * @return A new <code>ArrayList</code> of <code>PNConstraint</code>.
     */
    protected static List<PNConstraint> initializeAdditionalConstraints() {
        List<PNConstraint> constraints = new ArrayList<>();
        constraints.add(new NoMixedParents());
        return constraints;
    }

    /**
     * @return An <code>ArrayList</code> of <code>NetworkType</code> where the
     *         algorithm can be applied: Bayesian networks and influence
     *         diagrams.
     */
    protected static List<NetworkType> initializeNetworkTypesApplicable() {
        List<NetworkType> networkTypes = new ArrayList<>();
        networkTypes.add(BayesianNetworkType.getUniqueInstance());
        networkTypes.add(InfluenceDiagramType.getUniqueInstance());
        networkTypes.add(MIDType.getUniqueInstance());
        return networkTypes;
    }

    //TODO: refactor and use maybe from network consistency?
    public static void checkEvaluability(ProbNet probNet) throws NotEvaluableNetworkException {
        boolean isApplicable;

        List<NetworkType> networkTypes = initializeNetworkTypesApplicable();

        isApplicable = false;
        NetworkType networkType = probNet.getNetworkType();
        // Check that there is a network type applicable equal to type of
        // probNet
        for (int i = 0; (i < networkTypes.size()) && !isApplicable; i++) {
            isApplicable = networkType == networkTypes.get(i);
        }

        if (!isApplicable) {
            throw new NotEvaluableNetworkException("Network type " + networkType.toString()
                    + "is not evaluable.");
        } else {
            // Check that the probNet satisfies the specific constraints of the
            // algorithm
            List<PNConstraint> additionalConstraints = initializeAdditionalConstraints();
            // Removed from the for en clause "&& isApplicable"
            //for (int i = 0; (i < additionalConstraints.size()) && isApplicable; i++) {
            // And then replacer to for..each
            /*
                        for (int i = 0; i < additionalConstraints.size(); i++) {
                PNConstraint pnConstraint = additionalConstraints.get(i);
                if (!pnConstraint.checkProbNet(probNet)) {
                    throw new NotEvaluableNetworkException("Constraint " + pnConstraint.toString()
                            + " is not satisfied by the network.");
                }
            }
             */
            for (PNConstraint pnConstraint : additionalConstraints) {
                if (!pnConstraint.checkProbNet(probNet)) {
                    throw new NotEvaluableNetworkException("Constraint " + pnConstraint.toString()
                            + " is not satisfied by the network.");
                }
            }
        }
    }
}
