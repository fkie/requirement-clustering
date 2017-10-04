package de.fraunhofer.fkie.evaluation.distancefunction;

import java.util.Map;

import mikera.vectorz.Vector;

public class ChebyshevDistance implements DistanceFunction {

	@Override
	public double distance(final Map<String, Double> v1Content, final Map<String, Double> v2Content) {
		double maxDist = 0;
		for (String key : v1Content.keySet()) {
			double dist = Math.abs(v1Content.get(key) - v2Content.get(key));
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		return maxDist;
	}

	@Override
	public String getName() {
		return "ChebyshevDistance";
	}

	@Override
	public double distanceZ(final Vector v1, final Vector v2) {
		double maxDist = 0;
		for (int i = 0; i < v1.length(); i++) {
			double dist = Math.abs(v1.get(i) - v2.get(i));
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		return maxDist;
	}

}
