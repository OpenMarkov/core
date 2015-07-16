package org.openmarkov.core.inference.heuristic;

import java.util.Collection;
import java.util.List;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public interface HeuristicFactory {

	EliminationHeuristic getHeuristic(ProbNet probNet, List<Collection<Variable>> variables);
}
