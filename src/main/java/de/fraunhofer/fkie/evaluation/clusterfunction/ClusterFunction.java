package de.fraunhofer.fkie.evaluation.clusterfunction;

import java.util.List;

import de.fraunhofer.fkie.evaluation.distancefunction.DistanceFunction;
import de.fraunhofer.fkie.evaluation.distancefunction.DistanceHandler;
import de.fraunhofer.fkie.evaluation.model.RequirementVector;

public interface ClusterFunction {
	/**
	 * 
	 * @return name of the cluster algorithm (may contain some parameters)
	 */
	public String getName();

	/**
	 * 
	 * @param dataSet
	 * @param func
	 * @return A list representing the resulting clusters
	 */
	public List<List<RequirementVector>> cluster(List<RequirementVector> dataSet, DistanceHandler func,
			List<Integer> givenCenters);

	public List<List<RequirementVector>> evaluationCluster(List<RequirementVector> dataSet, DistanceHandler func);
}
