package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class EuclideanDistance implements DistanceFunction {

	@Override
	public synchronized double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double dist = 0;
		for (String key : v1Content.keySet()) {
			dist += Math.pow((v1Content.get(key) - v2Content.get(key)), 2);
		}
		return Math.sqrt(dist);
	}

	@Override
	public String getName() {
		return "EuclideanDistance";
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double dist = 0;
		for (int i = 0; i < v1.length(); i++) {
			dist += Math.pow(v1.get(i) - v2.get(i), 2);
		}
		return Math.sqrt(dist);
	}

}
