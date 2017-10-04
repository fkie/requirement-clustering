package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public interface DistanceFunction {

	/**
	 *
	 * @return the name of the distance function
	 */
	public String getName();

	/**
	 *
	 * @param v1
	 * @param v2
	 * @return the distance of v1 to v2 (specific to the distance function)
	 */
	double distance(Map<String, Double> v1Content, Map<String, Double> v2Content);

	double distanceZ(Vector v1, Vector v2);
}
