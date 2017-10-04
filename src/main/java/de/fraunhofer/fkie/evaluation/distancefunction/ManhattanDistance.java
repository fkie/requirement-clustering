package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class ManhattanDistance implements DistanceFunction {

	@Override
	public String getName() {
		return "ManhattanDistance";
	}

	@Override
	public double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double dist = 0;
		for (String key : v1Content.keySet()) {
			dist += Math.abs(v1Content.get(key) - v2Content.get(key));
		}
		return dist;
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double dist = 0;
		for (int i = 0; i < v1.length(); i++) {
			dist += Math.abs(v1.get(i) - v2.get(i));
		}
		return dist;
	}

}
